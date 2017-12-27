/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.out;

import edu.umd.cs.findbugs.annotations.NonNull;
import fredboat.dike.Config;
import fredboat.dike.io.out.handle.*;
import fredboat.dike.session.Session;
import fredboat.dike.util.JsonHandler;
import fredboat.dike.util.OpCodes;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LocalGateway extends WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(LocalGateway.class);

    private final ArrayList<OutgoingHandler> handlers = new ArrayList<>();
    private final JsonHandler jsonHandler;
    private ConcurrentHashMap<String, LocalSocketContext> socketContexts = new ConcurrentHashMap<>();
    private final Config config;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public LocalGateway(Config config) {
        super(new InetSocketAddress(config.host(), config.port()));
        this.config = config;

        jsonHandler = new JsonHandler();

        // Opcodes that are received only to the client will be ignored by OutNOPHandler
        handlers.add(OpCodes.OP_0_DISPATCH, new OutNOPHandler(this));
        handlers.add(OpCodes.OP_1_HEARTBEAT, new OutHeartbeatHandler(this));
        handlers.add(OpCodes.OP_2_IDENTIFY, new OutIdentifyHandler(this));
        handlers.add(OpCodes.OP_3_PRESENCE, new OutForwardingHandler(this));
        handlers.add(OpCodes.OP_4_VOICE_STATE, new OutForwardingHandler(this));
        handlers.add(OpCodes.OP_5_VOICE_PING, new OutForwardingHandler(this));
        handlers.add(OpCodes.OP_6_RESUME, new OutResumeHandler(this));
        handlers.add(OpCodes.OP_7_RECONNECT, new OutNOPHandler(this));
        handlers.add(OpCodes.OP_8_REQUEST_MEMBERS, new OutForwardingHandler(this));
        handlers.add(OpCodes.OP_9_INVALIDATE_SESSION, new OutNOPHandler(this));
        handlers.add(OpCodes.OP_10_HELLO, new OutNOPHandler(this));
        handlers.add(OpCodes.OP_11_HEARTBEAT_ACK, new OutNOPHandler(this));
        handlers.add(OpCodes.OP_12_GUILD_SYNC, new OutForwardingHandler(this));

        executor.scheduleAtFixedRate(new BotHeartbeatWatchdog(this), 0, 50, TimeUnit.SECONDS);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        log.info("Opened connection from " + conn.getRemoteSocketAddress());
        socketContexts.put(conn.getResourceDescriptor(), new LocalSocketContext(conn));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (reason != null && !reason.isEmpty()) {
            log.info("Closed connection from " + conn.getRemoteSocketAddress()
                    + " :: remote = " + remote + " :: reason = " + reason);
        } else {
            log.info("Closed connection from " + conn.getRemoteSocketAddress()
                    + " :: remote = " + remote);
        }

        Session session = getSession(conn);
        if (session != null) {
            session.onLocalSocketDisconnect();
        }

        socketContexts.remove(conn.getResourceDescriptor());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        log.info(conn.getRemoteSocketAddress() + " " + message);

        int op = jsonHandler.getOp(message);

        if (op == -1) throw new RuntimeException("Unable to parse op: " + message);

        OutgoingHandler outgoingHandler = handlers.get(op);
        if (outgoingHandler != null) {
            try {
                outgoingHandler.handle(conn, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Unhandled opcode: " + op + " Forwarding the message");
            forward(conn, message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error("Caught exception in websocket", ex);
    }

    @Override
    public void onStart() {
        log.info("Started listening on " + getAddress());
    }

    public void forward(@Nonnull WebSocket conn, @Nonnull String string) {
        //noinspection ConstantConditions
        getSession(conn).sendDiscord(string);
    }

    @Nullable
    public Session getSession(WebSocket conn) {
        return socketContexts.get(conn.getResourceDescriptor()).getSession();
    }

    public void setSession(@NonNull WebSocket conn, @Nullable Session session) {
        socketContexts.get(conn.getResourceDescriptor()).setSession(session);
    }

    @Nullable
    public LocalSocketContext getContext(@NonNull WebSocket ws) {
        return socketContexts.get(ws.getResourceDescriptor());
    }

    Collection<LocalSocketContext> getContexts() {
        return socketContexts.values();
    }

    public Config getConfig() {
        return config;
    }
}
