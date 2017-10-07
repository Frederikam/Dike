/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.cache;

import com.neovisionaries.ws.client.WebSocketException;
import fredboat.dike.io.in.DiscordGateway;
import fredboat.dike.util.GatewayUtil;
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

    Session(ShardIdentifier identifier) {
        this.identifier = identifier;

        try {
            discordGateway = new DiscordGateway(this, new URI(GatewayUtil.getGateway()));
        } catch (URISyntaxException | WebSocketException | IOException e) {
            throw new RuntimeException("Failed to open gateway connection", e);
        }
    }

    public void sendDiscord(String message) {
        outboundQueue.add(message);
    }

    public void sendLocal(String message) {

    }

    public ShardIdentifier getIdentifier() {
        return identifier;
    }
}
