package fredboat.dike.io.rest;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fredboat.dike.util.Const;
import fredboat.dike.util.Ratelimit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RestRequester {

    private static LoadingCache<String, RestRequester> instances = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, RestRequester>() {
                @Override
                public RestRequester load(@SuppressWarnings("NullableProblems") String token) {
                    return new RestRequester(token);
                }
            });
    private static final Logger log = LoggerFactory.getLogger(RestRequester.class);

    private String token;
    private OkHttpClient http = new OkHttpClient();
    private ConcurrentHashMap<Route, Ratelimit> ratelimits = new ConcurrentHashMap<>();

    private RestRequester(String token) {
        this.token = token;
    }

    public Response requestSync(Route route, String... args) throws IOException, InterruptedException {
        Request request = new Request.Builder()
                .header("Authorization", "Bot " + token)
                .header("User-Agent", Const.USER_AGENT)
                .url(Const.API_BASE_URL + String.format(route.getRoute(), (Object) args))
                .build();

        Ratelimit ratelimit = ratelimits.computeIfAbsent(route, __ -> new Ratelimit(
                        10000,
                        ___ -> Instant.now().plusSeconds(300), // Typically gets set, this is a start guess
                        ____ -> {
                            log.warn("The {} route hit the ratelimit!", route);
                        }
                )
        );

        ratelimit.acquire();

        Response res = http.newCall(request).execute();

        if (res.code() < 200 && res.code() > 299) {
            throw new RuntimeException("Non-success status code for route " + route + " with body "
                    + Objects.requireNonNull(res.body()).string());
        }

        try {
            ratelimit.setLimit(Integer.parseInt(Objects.requireNonNull(res.header("X-RateLimit-Limit"))));
            ratelimit.setRemaining(Integer.parseInt(Objects.requireNonNull(res.header("X-RateLimit-Remaining"))));
            ratelimit.setResetTime(Instant.ofEpochSecond(Long.parseLong(Objects.requireNonNull(res.header("X-RateLimit-Reset")))));
        } catch (NullPointerException npe) {
            log.warn("Did not receive ratelimit info for route " + route);
        }

        return res;
    }

    public static RestRequester instance(String token) {
        try {
            return instances.get(token);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
