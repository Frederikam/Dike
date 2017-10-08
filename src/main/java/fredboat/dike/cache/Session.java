/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.cache;

import com.neovisionaries.ws.client.WebSocketException;
import fredboat.dike.io.in.DiscordGateway;
import fredboat.dike.io.in.DiscordQueuePoller;
import fredboat.dike.io.out.LocalGateway;
import fredboat.dike.util.GatewayUtil;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;

public class Session {

    private static final Logger log = LoggerFactory.getLogger(Session.class);

    private ShardIdentifier identifier;
    private LinkedBlockingQueue<String> outboundQueue = new LinkedBlockingQueue<>();
    private DiscordGateway discordGateway;
    @SuppressWarnings("FieldCanBeLocal")
    private final DiscordQueuePoller poller;
    private LocalGateway localGateway;
    private WebSocket localSocket = null;

    Session(ShardIdentifier identifier, LocalGateway localGateway, WebSocket localSocket, String op2) {
        this.identifier = identifier;
        this.localGateway = localGateway;
        this.localSocket = localSocket;

        try {
            discordGateway = new DiscordGateway(this, new URI(GatewayUtil.getGateway()), op2);
        } catch (URISyntaxException | WebSocketException | IOException e) {
            throw new RuntimeException("Failed to open gateway connection", e);
        }

        poller = new DiscordQueuePoller(this, outboundQueue);
        poller.start();
    }

    public void sendDiscord(String message) {
        outboundQueue.add(message);
    }

    public void sendLocal(String message) {
        localSocket.send(message);
    }

    public ShardIdentifier getIdentifier() {
        return identifier;
    }

    public DiscordGateway getDiscordGateway() {
        return discordGateway;
    }

    public WebSocket getLocalSocket() {
        return localSocket;
    }

    public void setLocalSocket(WebSocket localSocket) {
        this.localSocket = localSocket;
    }
}
