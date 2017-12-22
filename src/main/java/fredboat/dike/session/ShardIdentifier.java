/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.session;

import fredboat.dike.io.rest.RestRequester;
import fredboat.dike.io.rest.Route;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ShardIdentifier {

    private static final Logger log = LoggerFactory.getLogger(ShardIdentifier.class);

    private static OkHttpClient http = new OkHttpClient();

    private final String token;
    private final long user;
    private final int shardId;
    private final int shardCount;

    private ShardIdentifier(String token, long user, int shardId, int shardCount) {
        this.token = token;
        this.user = user;
        this.shardId = shardId;
        this.shardCount = shardCount;
    }

    public static ShardIdentifier getFromToken(String token, int shardId, int shardCount) throws IOException {

        Response response = null;
        try {
            response = RestRequester.instance(token).requestSync(Route.USER_AT_ME);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

        if (response.code() != 200) {
            log.warn("Not code 200: " + response.code());
        }

        //noinspection ConstantConditions
        long user = new JSONObject(response.body().string()).getLong("id");

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

    @Override
    public String toString() {
        return "ShardIdentifier{" +
                "user=" + user +
                ", shardId=" + shardId +
                ", shardCount=" + shardCount +
                '}';
    }

    public String toStringShort() {
        return "[" + shardId + " / " + shardCount + "]";
    }
}
