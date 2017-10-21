/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.session.cache;

import java.util.concurrent.ConcurrentHashMap;

public class Guild {

    private ConcurrentHashMap<String, String> voiceChannels = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> roles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();

    Guild(String payload) {

    }

}
