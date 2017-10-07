/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.out;

import fredboat.dike.io.Codes;
import fredboat.dike.io.out.handle.OutForwardingHandler;
import fredboat.dike.io.out.handle.OutgoingHandler;
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

    private final ArrayList<OutgoingHandler> handlers = new ArrayList<>();
    private final INotationHandler notationHandler;

    public LocalGateway() {
        super(new InetSocketAddress(9999));

        notationHandler = new JsonHandler();

        handlers.add(Codes.OP_0_DISPATCH, new OutForwardingHandler(this));
        handlers.add(Codes.OP_1_HEARTBEAT, new OutForwardingHandler(this));
        handlers.add(Codes.OP_2_IDENTIFY, new OutForwardingHandler(this));
        handlers.add(Codes.OP_3_PRESENCE, new OutForwardingHandler(this));
        handlers.add(Codes.OP_4_VOICE_STATE, new OutForwardingHandler(this));
        handlers.add(Codes.OP_5_VOICE_PING, new OutForwardingHandler(this));
        handlers.add(Codes.OP_6_RESUME, new OutForwardingHandler(this));
        handlers.add(Codes.OP_7_RECONNECT, new OutForwardingHandler(this));
        handlers.add(Codes.OP_8_REQUEST_MEMBERS, new OutForwardingHandler(this));
        handlers.add(Codes.OP_9_INVALIDATE_SESSION, new OutForwardingHandler(this));
        handlers.add(Codes.OP_10_HELLO, new OutForwardingHandler(this));
        handlers.add(Codes.OP_11_HEARTBEAT_ACK, new OutForwardingHandler(this));
        handlers.add(Codes.OP_12_GUILD_SYNC, new OutForwardingHandler(this));
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

        OutgoingHandler outgoingHandler = handlers.get(op);
        if (outgoingHandler != null) {
            outgoingHandler.handle(message);
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
