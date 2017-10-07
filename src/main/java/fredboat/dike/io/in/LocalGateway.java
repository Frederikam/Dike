/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in;

import fredboat.dike.io.Codes;
import fredboat.dike.io.in.handle.InForwardingHandler;
import fredboat.dike.io.in.handle.IncomingHandler;
import fredboat.dike.notation.INotationHandler;
import fredboat.dike.notation.JsonHandler;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class LocalGateway extends WebSocketServer {

    private final ArrayList<IncomingHandler> handlers = new ArrayList<>();
    private final INotationHandler notationHandler;

    public LocalGateway() {
        super(new InetSocketAddress(9999));

        notationHandler = new JsonHandler();

        handlers.add(Codes.OP_0_DISPATCH, new InForwardingHandler(this));
        handlers.add(Codes.OP_1_HEARTBEAT, new InForwardingHandler(this));
        handlers.add(Codes.OP_2_IDENTIFY, new InForwardingHandler(this));
        handlers.add(Codes.OP_3_PRESENCE, new InForwardingHandler(this));
        handlers.add(Codes.OP_4_VOICE_STATE, new InForwardingHandler(this));
        handlers.add(Codes.OP_5_VOICE_PING, new InForwardingHandler(this));
        handlers.add(Codes.OP_6_RESUME, new InForwardingHandler(this));
        handlers.add(Codes.OP_7_RECONNECT, new InForwardingHandler(this));
        handlers.add(Codes.OP_8_REQUEST_MEMBERS, new InForwardingHandler(this));
        handlers.add(Codes.OP_9_INVALIDATE_SESSION, new InForwardingHandler(this));
        handlers.add(Codes.OP_10_HELLO, new InForwardingHandler(this));
        handlers.add(Codes.OP_11_HEARTBEAT_ACK, new InForwardingHandler(this));
        handlers.add(Codes.OP_12_GUILD_SYNC, new InForwardingHandler(this));
    }

    private static final Logger log = LoggerFactory.getLogger(LocalGateway.class);

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        log.info("Opened connection from " + conn.getRemoteSocketAddress());
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
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        log.info(conn.getRemoteSocketAddress() + " " + message);

        int op = notationHandler.getOp(message);

        if (op == -1) throw new RuntimeException("Unable to parse op: " + message);

        IncomingHandler incomingHandler = handlers.get(op);
        if (incomingHandler != null) {
            incomingHandler.handle(message);
        } else {
            log.warn("Unhandled opcode: " + op + " Forwarding the message");
            forward(message);
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

    public void forward(String string) {
        //TODO
    }

}
