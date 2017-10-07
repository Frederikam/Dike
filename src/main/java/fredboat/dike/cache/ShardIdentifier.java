/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import fredboat.dike.util.Const;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

public class ShardIdentifier {

    private final String token;
    private final long user;
    private final int shardId;
    private final int shardCount;

    public ShardIdentifier(String token, long user, int shardId, int shardCount) {
        this.token = token;
        this.user = user;
        this.shardId = shardId;
        this.shardCount = shardCount;
    }

    public static ShardIdentifier getFromToken(String token, int shardId, int shardCount) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Request request = new Request.Builder()
                .url(Const.API_GET_CURRENT_USER)
                .header("Authorization", "Bot " + token)
                .header("User-Agent", Const.USER_AGENT)
                .build();

        Response response = client.newCall(request).execute();
        //noinspection ConstantConditions
        long user = new JSONObject(response.body()).getLong("id");

        return new ShardIdentifier(token, user, shardId, shardCount);
    }

    public String getToken() {
        return token;
    }

    public long getUser() {
        return user;
    }

    public int getShardId() {
        return shardId;
    }

    public int getShardCount() {
        return shardCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShardIdentifier that = (ShardIdentifier) o;

        if (user != that.user) return false;
        //noinspection SimplifiableIfStatement
        if (shardId != that.shardId) return false;
        return shardCount == that.shardCount;
    }

    @Override
    public int hashCode() {
        int result = (int) (user ^ (user >>> 32));
        result = 31 * result + shardId;
        result = 31 * result + shardCount;
        return result;
    }
}
