/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.cache;

import fredboat.dike.io.out.LocalGateway;
import org.java_websocket.WebSocket;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    public static final SessionManager INSTANCE = new SessionManager();

    private ConcurrentHashMap<ShardIdentifier, Session> sessions = new ConcurrentHashMap<>();

    public Session getSession(ShardIdentifier identifier) {
        return sessions.get(identifier);
    }

    public Session createSession(ShardIdentifier identifier, LocalGateway localGateway, WebSocket localSocket, String op2) {
        Session session = new Session(identifier, localGateway, localSocket, op2);
        sessions.put(identifier, session);
        return session;
    }

    void onInvalidate(Session session) {
        sessions.remove(session.getIdentifier());
    }

}
