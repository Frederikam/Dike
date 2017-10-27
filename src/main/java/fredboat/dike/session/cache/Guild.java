/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.session.cache;

import com.jsoniter.ValueType;
import com.jsoniter.any.Any;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Guild {

    private final Cache cache;

    private Any d;
    private ConcurrentHashMap<Long, Any> channels = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Any> members = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Any> roles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Any> emojis = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Any> voiceStates = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Any> presences = new ConcurrentHashMap<>();

    Guild(Cache cache, Any d) {
        this.cache = cache;
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

        for (Any payload : d.get("voice_states").asList()) {
            setVoiceStates(payload);
        }

        for (Any payload : d.get("presences").asList()) {
            // Set directly from here because the constructor guarantees that the user is a member
            presences.put(payload.get("user").get("id").toLong(), payload);
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
                presences.remove(id);
                break;
            case ROLE:
                channels.remove(id);
                break;
            case EMOJI:
                emojis.remove(id);
                break;
        }
    }

    public void setVoiceStates(Any payload) {
        long id = payload.get("user_id").toLong();

        if (payload.get("channel_id").valueType() == ValueType.NULL) {
            // No channel means that the user left
            voiceStates.remove(id);
        } else {
            voiceStates.put(id, payload);
        }
    }

    public void setPresence(Any payload) {
        // Discord updates a presences AFTER GUILD_MEMBER_REMOVE.
        // We will check for this to avoid leaking memory
        long id = payload.get("user").get("id").toLong();

        if (members.containsKey(id))
            presences.put(id, payload);
    }

    /* Dispatches */

    List<Dispatch> computeDispatches() {
        List<Dispatch> list = new LinkedList<>();

        list.add(computeGuildCreate());

        return list;
    }

    private Dispatch computeGuildCreate() {
        Map<String, Any> map = new HashMap<>(d.asMap());
        
        boolean large = cache.getLargeThreshold() <= members.size();
        map.put("large", Any.wrap(large));

        List<Any> users = new LinkedList<>();

        if (large) {
            for (Map.Entry<Long, Any> entry : members.entrySet()) {
                Any presence = presences.get(entry.getKey());
                if (presence != null
                        && !presence.get("status").toString().equals("offline")) {
                    users.add(entry.getValue());
                }
            }
        } else {
            users.addAll(members.values());
        }

        map.put("members", Any.wrap(users));
        map.put("channels", Any.wrap(channels.values()));
        map.put("roles", Any.wrap(roles.values()));
        map.put("voice_states", Any.wrap(voiceStates.values()));
        map.put("presences", Any.wrap(presences.values()));

        return new Dispatch("GUILD_CREATE", map);
    }

}
