/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class LocalGateway extends WebSocketServer {

    public LocalGateway() {
        super(new InetSocketAddress(9999));
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
        log.trace(conn.getRemoteSocketAddress() + " " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error("Caught exception in websocket", ex);
    }

    @Override
    public void onStart() {

    }
}
