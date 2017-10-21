/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.session.cache;

import com.jsoniter.any.Any;

import java.util.concurrent.ConcurrentHashMap;

public class Guild {

    private Any d;
    private ConcurrentHashMap<Long, Any> channels = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Any> members = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Any> roles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Any> emojis = new ConcurrentHashMap<>();

    Guild(Any d) {
        this.d = d;
        for (Any payload : d.get("channels").asList()) {
            createEntity(EntityType.CHANNEL, payload);
        }

        for (Any payload : d.get("members").asList()) {
            createEntity(EntityType.MEMBER, payload);
        }

        for (Any payload : d.get("roles").asList()) {
            createEntity(EntityType.ROLE, payload);
        }

        for (Any payload : d.get("emojis").asList()) {
            createEntity(EntityType.EMOJI, payload);
        }
    }

    public void update(Any d) {
        this.d = d;

        // Just for good measure. These arrays are present in GUILD_UPDATE unlike members and channels
        for (Any payload : d.get("roles").asList()) {
            createEntity(EntityType.ROLE, payload);
        }
        for (Any payload : d.get("emojis").asList()) {
            createEntity(EntityType.EMOJI, payload);
        }
    }

    public void createEntity(EntityType type, Any payload) {
        long id = payload.get("id").toLong();

        switch (type) {
            case CHANNEL:
                channels.put(id, payload);
                break;
            case MEMBER:
                members.put(id, payload);
                break;
            case ROLE:
                roles.put(id, payload);
                break;
            case EMOJI:
                emojis.put(id, payload);
                break;
        }
    }

    public void deleteEntity(EntityType type, Any payload) {
        long id = payload.get("id").toLong();

        switch (type) {
            case CHANNEL:
                channels.remove(id);
                break;
            case MEMBER:
                members.remove(id);
                break;
            case ROLE:
                channels.remove(id);
                break;
            case EMOJI:
                emojis.remove(id);
                break;
        }
    }

}
