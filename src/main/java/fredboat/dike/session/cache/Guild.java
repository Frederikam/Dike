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
    private ConcurrentHashMap<Long, Any> roles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Any> users = new ConcurrentHashMap<>();

    Guild(Any d) {
        this.d = d;
        for (Any channel : d.get("channels").asList()) {
            createChannel(channel);
        }
    }

    public void createChannel(Any payload) {
        channels.put(Long.parseLong(payload.get("id").as(String.class)), payload);
    }

    public void deleteChannel(Any payload) {
        channels.remove(Long.parseLong(payload.get("id").as(String.class)));
    }

}
