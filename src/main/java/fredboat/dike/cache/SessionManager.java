/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.cache;

import fredboat.dike.io.out.LocalGateway;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    public static final SessionManager INSTANCE = new SessionManager();

    private ConcurrentHashMap<ShardIdentifier, Session> sessions = new ConcurrentHashMap<>();

    public Session getSession(ShardIdentifier identifier) {
        return sessions.get(identifier);
    }

    public Session createSession(ShardIdentifier identifier, LocalGateway localGateway, String op2) {
        Session session = new Session(identifier);
        sessions.put(identifier, session);
        return session;
    }

}
