/*
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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
