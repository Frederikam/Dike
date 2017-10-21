/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import com.jsoniter.JsonIterator;
import fredboat.dike.io.in.DiscordGateway;
import fredboat.dike.session.cache.Cache;
import org.json.JSONObject;

import java.io.IOException;

public class InDispatchHandler extends IncomingHandler {

    public InDispatchHandler(DiscordGateway discordGateway) {
        super(discordGateway);
        this.cache = discordGateway.getSession().getCache();
    }

    private long sequence = -1;
    private String sessionId = null;
    private final Cache cache;

    @Override
    public void handle(String message) throws IOException {
        JsonIterator iter = JsonIterator.parse(message);

        String type = null;

        for (String field = iter.readObject(); field != null; field = iter.readObject()) {
            switch (field) {
                case "s":
                    sequence = iter.readInt();
                    continue;
                case "t":
                    type = iter.readString();
                    continue;
                default:
                    iter.skip();
            }
        }

        //TODO: Cache
        assert type != null;
        switch (type) {
            case "GUILD_CREATE":
                cache.createGuild(message);
                break;
            case "GUILD_DELETE":
                cache.deleteGuild(message);
                break;
            case "READY":
                discordGateway.setState(DiscordGateway.State.CONNECTED);
                sessionId = new JSONObject(message).getJSONObject("d").getString("session_id");
                break;
            case "RESUMED":
                discordGateway.setState(DiscordGateway.State.CONNECTED);
                break;
        }

        discordGateway.forward(message);
    }

    long getSequence() {
        return sequence;
    }

    String getSessionId() {
        return sessionId;
    }
}
