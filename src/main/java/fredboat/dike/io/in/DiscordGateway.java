/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import fredboat.dike.cache.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class DiscordGateway extends WebSocketAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscordGateway.class);

    private final Session session;
    private final WebSocket socket;

    public DiscordGateway(Session session, URI uri) throws IOException, WebSocketException {
        this.session = session;


        socket = new WebSocketFactory()
                .createSocket(uri)
                .addListener(this)
                .addHeader("Accept-Encoding", "gzip");

        socket.connect();
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        log.info(text);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        log.info("Connected to " + websocket.getURI());
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        String str = "Got disconnected from websocket by "
                + (closedByServer ? "server" : "client")
                + "!";

        if (serverCloseFrame != null) {
            str += "\n\tRemote code: " + serverCloseFrame.getCloseCode();
            str += "\n\tRemote reason: " + serverCloseFrame.getCloseReason();
        }
        if (clientCloseFrame != null) {
            str += "\n\tClient code: " + clientCloseFrame.getCloseCode();
            str += "\n\tClient reason: " + clientCloseFrame.getCloseReason();
        }

        log.info(str);
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        log.error("Error in websocket", cause);
    }
}
