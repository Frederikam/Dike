/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.session;

import com.neovisionaries.ws.client.WebSocketException;
import edu.umd.cs.findbugs.annotations.NonNull;
import fredboat.dike.io.in.DiscordGateway;
import fredboat.dike.io.in.DiscordQueuePoller;
import fredboat.dike.io.out.LocalGateway;
import fredboat.dike.session.cache.Cache;
import fredboat.dike.session.cache.Dispatch;
import fredboat.dike.util.GatewayUtil;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Session {

    private static final Logger log = LoggerFactory.getLogger(Session.class);

    private ShardIdentifier identifier;
    private LinkedBlockingQueue<String> outboundQueue = new LinkedBlockingQueue<>();
    private final DiscordGateway discordGateway;
    @SuppressWarnings("FieldCanBeLocal")
    private final DiscordQueuePoller poller;
    private LocalGateway localGateway;
    private WebSocket localSocket;
    private final Cache cache = new Cache();
    private AtomicLong clientSequence = new AtomicLong(0);
    /**
     * The time at the point we were disconnected from the bot, or epoch if this has yet to happen.
     */
    private Instant timeBotDisconnected = Instant.EPOCH;

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
        if (!localSocket.isOpen()) return;

        localSocket.send(message);
    }

    public ShardIdentifier getIdentifier() {
        return identifier;
    }

    public DiscordGateway getDiscordGateway() {
        return discordGateway;
    }

    public boolean isBotConnected() {
        return localSocket != null && localSocket.isOpen();
    }

    @Nullable
    public WebSocket getLocalSocket() {
        return localSocket;
    }

    public Instant getTimeBotDisconnected() {
        return timeBotDisconnected;
    }

    public void onBotDisconnect() {
        localSocket = null;
        timeBotDisconnected = Instant.now();
    }

    public void changeLocalSocket(WebSocket localSocket) {
        assert !this.localSocket.isOpen() : "Cannot change local socket if one already is open!";
        clientSequence.set(0);

        if (discordGateway.getState() != DiscordGateway.State.CONNECTED) {
            log.warn("To change socket the connection must be {} but it is {}! Waiting for CONNECTED status...");

            while (discordGateway.getState() != DiscordGateway.State.CONNECTED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        log.info("Resuming session from Dike cache for session " + identifier.toStringShort());

        this.localSocket = localSocket;

        /* Send dispatches */
        synchronized (cache) {
            for (Dispatch dispatch : cache.computeDispatches()) {
                sendDispatch(dispatch);
            }
        }
    }

    /**
     * Send a dispatch to the client
     *
     * @param dispatch the Dispatch to send
     */
    public void sendDispatch(Dispatch dispatch) {
        localSocket.send(dispatch.wrap(clientSequence.getAndIncrement()));
    }

    @NonNull
    public Cache getCache() {
        return cache;
    }

    void destroy() {
        discordGateway.setState(DiscordGateway.State.SHUTDOWN);
        discordGateway.getSocket().sendClose(1000);

        if (localSocket != null) {
            localSocket.closeConnection(1000, "Dike session destroyed");
        }

        SessionManager.INSTANCE.getSessions().remove(this);
    }
}
