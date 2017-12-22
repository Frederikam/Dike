/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.session;

import fredboat.dike.Config;
import fredboat.dike.Launcher;
import fredboat.dike.io.out.LocalGateway;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    public static final SessionManager INSTANCE = new SessionManager();
    @SuppressWarnings("FieldCanBeLocal")
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Session killer"));
    private ConcurrentHashMap<ShardIdentifier, Session> sessions = new ConcurrentHashMap<>();

    private SessionManager() {
        executor.scheduleAtFixedRate(() -> {
            try {
                killOldSessions();
            } catch (Exception e) {
                log.error("Exception while looking for old session to kill", e);
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    @Nullable
    public Session getSession(ShardIdentifier identifier) {
        return sessions.get(identifier);
    }

    public Session createSession(ShardIdentifier identifier, LocalGateway localGateway, WebSocket localSocket, String op2) {
        Session session = new Session(identifier, localGateway, localSocket, op2);
        sessions.put(identifier, session);
        return session;
    }

    public void invalidate(Session session) {
        sessions.remove(session.getIdentifier());
        session.onShutdown();
    }

    private void killOldSessions() {
        // Sort all sessions into maps of bot users
        HashMap<Long, List<Session>> sessionsByBot = new HashMap<>();
        for (Session session : sessions.values()) {
            sessionsByBot.computeIfAbsent(session.getIdentifier().getUser(), aLong -> new LinkedList<>())
                    .add(session);
        }

        Config config = Launcher.getConfig();

        // For each user
        sessionsByBot.forEach((botId, sessions) -> {
            // Determine highest max shard count for this user
            int maxShardCount = 1;

            for (Session session : sessions)
                if (session.getIdentifier().getShardCount() > maxShardCount)
                    maxShardCount = session.getIdentifier().getShardCount();

            // Now see if it is time to invalidate any sessions
            for (Session session : sessions) {
                if (session.getLocalSocket().isOpen()) continue;

                long secondsDisconnected = Instant.now()
                        .minusSeconds(session.getLastTimeLocalDisconnected().getEpochSecond())
                        .getEpochSecond();
                if (session.getIdentifier().getShardCount() == maxShardCount && secondsDisconnected > config.timeoutIdle()) {
                    log.info("Invalidating {} for being idle too long", session);
                    invalidate(session);
                } else if (secondsDisconnected > config.timeoutIdleFormerGen()) {
                    log.info("Invalidating {} for being idle too long and being shadowed by bigger shards counts", session);
                    invalidate(session);
                }
            }
        });
    }

}
