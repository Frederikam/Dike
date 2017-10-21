/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import fredboat.dike.io.in.DiscordGateway;
import fredboat.dike.session.cache.Cache;
import fredboat.dike.session.cache.EntityType;
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

        EntityType entityType = null;
        assert type != null;
        switch (type) {
            case "CHANNEL_CREATE":
            case "CHANNEL_DELETE":
                entityType = EntityType.CHANNEL;
                break;
            case "MEMBER_ADD":
            case "MEMBER_REMOVE":
                entityType = EntityType.MEMBER;
                break;
            case "GUILD_ROLE_CREATE":
            case "GUILD_ROLE_DELETE":
                entityType = EntityType.ROLE;
                break;
            case "GUILD_EMOJI_CREATE":
            case "GUILD_EMOJI_DELETE":
                entityType = EntityType.EMOJI;
                break;
        }

        if (entityType != null) {
            switch (type) {
                case "CHANNEL_CREATE":
                case "MEMBER_ADD":
                case "GUILD_ROLE_CREATE":
                case "GUILD_EMOJI_CREATE":
                    Any dCreate = JsonIterator.deserialize(message).get("d");
                    cache.getGuild(dCreate.get("guild_id").toLong())
                            .createEntity(entityType, dCreate);
                    break;
                default:
                    Any dDelete = JsonIterator.deserialize(message).get("d");
                    cache.getGuild(dDelete.get("guild_id").toLong())
                            .deleteEntity(entityType, dDelete);
                    break;
            }
        }

        /* Handle all other switch cases */
        switch (type) {
            case "GUILD_CREATE":
                cache.createGuild(JsonIterator.deserialize(message).get("d"));
                break;
            case "GUILD_DELETE":
                cache.deleteGuild(JsonIterator.deserialize(message).get("d"));
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
