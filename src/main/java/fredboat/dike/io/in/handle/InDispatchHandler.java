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
import fredboat.dike.session.cache.Guild;
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
        if (type == null) throw new RuntimeException("Received invalid OP 0 with no type field!");
        switch (type) {
            case "CHANNEL_CREATE":
            case "CHANNEL_UPDATE":
            case "CHANNEL_DELETE":
                entityType = EntityType.CHANNEL;
                break;
            case "GUILD_MEMBER_ADD":
            case "GUILD_MEMBER_REMOVE":
                entityType = EntityType.MEMBER;
                break;
            case "GUILD_ROLE_CREATE":
            case "GUILD_ROLE_UPDATE":
            case "GUILD_ROLE_DELETE":
                entityType = EntityType.ROLE;
                break;
            case "GUILD_EMOJI_CREATE":
            case "GUILD_EMOJI_UPDATE":
            case "GUILD_EMOJI_DELETE":
                entityType = EntityType.EMOJI;
                break;
        }

        if (entityType != null) switch (type) {
            case "CHANNEL_CREATE":
            case "GUILD_MEMBER_ADD":
            case "GUILD_ROLE_CREATE":
            case "GUILD_EMOJI_CREATE":

            case "CHANNEL_UPDATE":
            case "GUILD_ROLE_UPDATE":
            case "GUILD_EMOJI_UPDATE":
                Any dCreate = JsonIterator.deserialize(message).get("d");
                Guild guild1 = cache.getGuild(dCreate.get("guild_id").toLong());

                if (entityType == EntityType.ROLE) {
                    // The role is wrapped in a JSON object together with the guild_id
                    dCreate = dCreate.get("role");
                }

                if (guild1 == null) throw new RuntimeException("Received " + type + " for unknown guild! d=" + dCreate);
                guild1.createEntity(entityType, dCreate);
                break;
            default:
                Any dDelete = JsonIterator.deserialize(message).get("d");
                Guild guild2 = cache.getGuild(dDelete.get("guild_id").toLong());

                if (guild2 == null) throw new RuntimeException("Received " + type + " for unknown guild! d=" + dDelete);
                guild2.deleteEntity(entityType, dDelete);
                break;
        }

        /* Handle all other switch cases */
        synchronized (cache) {
            switch (type) {
                case "READY":
                    discordGateway.setState(DiscordGateway.State.CONNECTED);
                    sessionId = new JSONObject(message).getJSONObject("d").getString("session_id");
                    cache.handleReadyEvent(JsonIterator.deserialize(message));
                    break;
                case "RESUMED":
                    discordGateway.setState(DiscordGateway.State.CONNECTED);
                    break;
                case "GUILD_CREATE":
                    cache.createGuild(JsonIterator.deserialize(message).get("d"));
                    break;
                case "GUILD_UPDATE":
                    Any dUpdate = JsonIterator.deserialize(message).get("d");
                    Guild guildUpdate = cache.getGuild(dUpdate.get("guild_id").toLong());

                    if (guildUpdate == null) throw new RuntimeException("Received " + type + " for unknown guild!");
                    guildUpdate.update(dUpdate);
                    break;
                case "GUILD_DELETE":
                    cache.deleteGuild(JsonIterator.deserialize(message).get("d"));
                    break;
                case "GUILD_MEMBERS_CHUNK":
                    Any dChunk = JsonIterator.deserialize(message).get("d");
                    Guild guildChunking = cache.getGuild(dChunk.get("guild_id").toLong());

                    if (guildChunking == null) throw new RuntimeException("Received " + type + " for unknown guild!");
                    for (Any any : dChunk.get("members").asList()) {
                        guildChunking.createEntity(EntityType.MEMBER, any);
                    }
                    break;
                case "VOICE_STATE_UPDATE":
                    Any dVoice = JsonIterator.deserialize(message).get("d");
                    Guild stateGuild = cache.getGuild(dVoice.get("guild_id").toLong());

                    if (stateGuild == null) throw new RuntimeException("Received " + type + " for unknown guild!");
                    stateGuild.setVoiceStates(dVoice);
                    break;
                case "PRESENCE_UPDATE":
                    Any dPres = JsonIterator.deserialize(message).get("d");
                    Guild guildPres = cache.getGuild(dPres.get("guild_id").toLong());

                    if (guildPres == null) throw new RuntimeException("Received " + type + " for unknown guild!");
                    guildPres.setPresence(dPres);
                    break;
                case "TYPING_START":
                    return; // Ignore, don't forward
            }
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
