/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.cache;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private ConcurrentHashMap<ShardIdentifier, Session> sessions = new ConcurrentHashMap<>();

}
