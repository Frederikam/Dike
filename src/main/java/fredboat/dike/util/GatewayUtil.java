/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GatewayUtil {

    private static final Logger log = LoggerFactory.getLogger(GatewayUtil.class);
    private static OkHttpClient http = new OkHttpClient();
    private static volatile String cachedUrl = null;

    public static String getGateway(boolean invalidateCache) {
        if (invalidateCache) cachedUrl = null;
        return getGateway();
    }

    public static String getGateway() {
        if (cachedUrl != null) return cachedUrl;

        Request request = new Request.Builder()
                .url(Const.API_GET_GATEWAY)
                .header("User-Agent", Const.USER_AGENT)
                .build();

        try {
            Response response = http.newCall(request).execute();

            //noinspection ConstantConditions
            cachedUrl = new JSONObject(response.body().string()).getString("url");
            return cachedUrl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
