/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in;

import com.neovisionaries.ws.client.*;
import fredboat.dike.io.in.handle.*;
import fredboat.dike.session.Session;
import fredboat.dike.session.cache.Cache;
import fredboat.dike.util.CloseCodes;
import fredboat.dike.session.IdentifyRatelimitHandler;
import fredboat.dike.util.JsonHandler;
import fredboat.dike.util.OpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.zip.DataFormatException;
import java.util.zip.InflaterOutputStream;

import static fredboat.dike.io.in.DiscordGateway.State.*;

public class DiscordGateway extends WebSocketAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscordGateway.class);

    private final ArrayList<IncomingHandler> handlers = new ArrayList<>();
    private final JsonHandler jsonHandler = new JsonHandler();
    private final Session session;
    private final URI url;
    private WebSocket socket;
    /**
     * If true we should not send any messages asides from OP 2 and OP 6
     */
    private volatile boolean locked = true;
    private volatile State state = INITIALIZING;
    private int failedConnectAttempts = 0;
    private final Cache cache;
    /**
     * See {@link IdentifyRatelimitHandler}
     */
    @Nullable
    private CountDownLatch identifyLatch = null;

    public DiscordGateway(Session session, URI url, String op2) throws IOException, WebSocketException {
        this.session = session;
        this.url = url;
        this.cache = session.getCache();

        handlers.add(OpCodes.OP_0_DISPATCH, new InDispatchHandler(this));
        handlers.add(OpCodes.OP_1_HEARTBEAT, new InNOPHandler(this)); // We may want to implement this
        handlers.add(OpCodes.OP_2_IDENTIFY, new InNOPHandler(this));
        handlers.add(OpCodes.OP_3_PRESENCE, new InNOPHandler(this));
        handlers.add(OpCodes.OP_4_VOICE_STATE, new InNOPHandler(this));
        handlers.add(OpCodes.OP_5_VOICE_PING, new InNOPHandler(this));
        handlers.add(OpCodes.OP_6_RESUME, new InNOPHandler(this));
        handlers.add(OpCodes.OP_7_RECONNECT, new InNOPHandler(this)); //TODO
        handlers.add(OpCodes.OP_8_REQUEST_MEMBERS, new InNOPHandler(this));
        handlers.add(OpCodes.OP_9_INVALIDATE_SESSION, new InInvalidateSessionHandler(this));
        handlers.add(OpCodes.OP_10_HELLO, new InHelloHandler(this, op2));
        handlers.add(OpCodes.OP_11_HEARTBEAT_ACK, new InForwardingHandler(this)); // We may want to implement this
        handlers.add(OpCodes.OP_12_GUILD_SYNC, new InNOPHandler(this));

        /* Wait for identify greenlight and connect */
        new Thread(() -> {
            try {
                setState(IDENTIFY_RATELIMIT);
                socket = new WebSocketFactory()
                        .createSocket(url)
                        .addListener(this)
                        .addHeader("Accept-Encoding", "gzip");

                identifyLatch = IdentifyRatelimitHandler.getInstance(session.getIdentifier().getUser()).acquire(this);
                setState(WAITING_FOR_HELLO_TO_IDENTIFY);

                socket.connect();
            } catch (IOException | InterruptedException | WebSocketException e) {
                log.error("Exception while connecting to Discord", e);
                setState(SHUTDOWN);
            }
        }, "connect-thread-"+session.getIdentifier().getUser()).start();
    }

    private void connect() throws IOException, WebSocketException {
        socket = new WebSocketFactory()
                .createSocket(url)
                .addListener(this)
                .addHeader("Accept-Encoding", "gzip");

        socket.connect();
    }

    /* Thanks JDA folks for this method */
    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws UnsupportedEncodingException, DataFormatException {
        //Thanks to ShadowLordAlpha and Shredder121 for code and debugging.
        //Get the compressed message and inflate it
        ByteArrayOutputStream out = new ByteArrayOutputStream(binary.length * 2);
        try (InflaterOutputStream decompressor = new InflaterOutputStream(out)) {
            decompressor.write(binary);
        } catch (IOException e) {
            throw (DataFormatException) new DataFormatException("Malformed").initCause(e);
        }

        // send the inflated message to the TextMessage method
        onTextMessage(websocket, out.toString("UTF-8"));
    }

    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
        thread.setName("Discord " + thread.getName() + " " + getSession().getIdentifier().toStringShort());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void onTextMessage(WebSocket websocket, String text) {
        try {
            log.info(text);

            int op = jsonHandler.getOp(text);

            if (op == -1) throw new RuntimeException("Unable to parse op: " + text);

            IncomingHandler incoming = handlers.get(op);
            if (incoming != null) {
                incoming.handle(text);
            } else {
                log.warn("Unhandled opcode: " + op + " Forwarding the message");
                forward(text);
            }
        } catch (Exception e) {
            log.info("Caught exception in websocket", e);
        }
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        log.info("Connected to " + websocket.getURI());
        failedConnectAttempts = 0;
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

        if (identifyLatch != null) identifyLatch.countDown(); // Prevent unnecessary waiting

        if (state == SHUTDOWN) return;

        boolean shouldResume = true;

        if (closedByServer && serverCloseFrame != null) {
            // Make sure we can actually recover
            if (serverCloseFrame.getCloseCode() == CloseCodes.AUTHENTICATION_FAILED.getCode()
                    || serverCloseFrame.getCloseCode() == CloseCodes.SHARDING_REQUIRED.getCode()) {
                log.error("Fatal remote close code " + serverCloseFrame.getCloseCode() + "!");
                setState(SHUTDOWN);
                return;
            }

            // Check if the session is still valid
            // Src: https://fred.moe/Twj.png and https://fred.moe/lnj.png
            shouldResume =
                    serverCloseFrame.getCloseCode() != CloseCodes.UNKNOWN_OPCODE.getCode()
                            && serverCloseFrame.getCloseCode() != CloseCodes.ALREADY_AUTHENTICATED.getCode()
                            && serverCloseFrame.getCloseCode() != CloseCodes.RESUME_INVALID_SESSION.getCode()
                            && serverCloseFrame.getCloseCode() != CloseCodes.INVALID_SEQ.getCode()
                            && serverCloseFrame.getCloseCode() != 4011; // Not sure what this is
        }

        if (!closedByServer
                && clientCloseFrame != null
                && clientCloseFrame.getCloseCode() == CloseCodes.GRACEFUL_CLOSE.getCode())
            shouldResume = false;

        if (state == IDENTIFYING
                || state == WAITING_FOR_HELLO_TO_IDENTIFY)
            shouldResume = false;

        // See what the OP 9 handler has to say
        if (((InInvalidateSessionHandler) handlers.get(OpCodes.OP_9_INVALIDATE_SESSION)).shouldIdentify())
            shouldResume = false;

        if (shouldResume) {
            setState(WAITING_FOR_HELLO_TO_RESUME);
            connect();
        } else {
            setState(IDENTIFY_RATELIMIT);
            new Thread(() -> {
                MDC.put("shard", session.getIdentifier().toStringShort());
                try {
                    identifyLatch = IdentifyRatelimitHandler.getInstance(session.getIdentifier().getUser()).acquire(this);
                } catch (InterruptedException e) {
                    log.error("Got interrupted while reconnecting shard");
                }
                setState(WAITING_FOR_HELLO_TO_IDENTIFY);
            }).start();
        }
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        log.error("Error in websocket", cause);
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        log.error("Error in websocket", cause);
    }

    @Override
    public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
        MDC.put("shard", session.getIdentifier().toStringShort());
    }

    public void forward(String message) {
        session.sendLocal(message);
    }

    public WebSocket getSocket() {
        return socket;
    }

    public boolean isLocked() {
        return state != CONNECTED;
    }

    public Session getSession() {
        return session;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        log.info(this.state + " -> " + state);

        if ((state == SHUTDOWN || state == IDENTIFYING) && identifyLatch != null)
            identifyLatch.countDown();

        if (this.state == SHUTDOWN && state != SHUTDOWN) {
            throw new IllegalStateException("Can't change state if we are shutdown!");
        }

        this.state = state;
    }

    public IncomingHandler getHandler(int op) {
        return handlers.get(op);
    }

    public enum State {
        /**
         * Initial state
         */
        INITIALIZING,
        /**
         * We are waiting for the green light to identify. This state is entered when reconnecting or initially connecting.
         */
        IDENTIFY_RATELIMIT,
        /**
         * We just started (or reconnected) and are waiting for OP 10 so we can identify
         */
        WAITING_FOR_HELLO_TO_IDENTIFY,
        /**
         * OP 2 was sent. We should be receiving dispatches soon
         */
        IDENTIFYING,
        /**
         * READY was received after identified, or RESUMED was received after resuming. Events are being received
         */
        CONNECTED,
        /**
         * We just opened the socket so we can resume and are waiting for OP 10
         */
        WAITING_FOR_HELLO_TO_RESUME,
        /**
         * We just sent OP 6 and should be replaying events again
         */
        RESUMING,
        /**
         * Something failed irrecoverably. Most likely cause is that our token got reset
         */
        SHUTDOWN

    }

}
