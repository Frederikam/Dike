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
import fredboat.dike.io.in.handle.InForwardingHandler;
import fredboat.dike.io.in.handle.InHelloHandler;
import fredboat.dike.io.in.handle.InInvalidateSessionHandler;
import fredboat.dike.io.in.handle.InNoOpHandler;
import fredboat.dike.io.in.handle.IncomingHandler;
import fredboat.dike.util.JsonHandler;
import fredboat.dike.util.OpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.InflaterOutputStream;

import static fredboat.dike.io.in.DiscordGateway.State.*;

public class DiscordGateway extends WebSocketAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscordGateway.class);

    private final ArrayList<IncomingHandler> handlers = new ArrayList<>();
    private final JsonHandler jsonHandler = new JsonHandler();
    private final Session session;
    private final WebSocket socket;
    /**
     * If true we should not send any messages asides from OP 2 and OP 6
     */
    private volatile boolean locked = true;

    private volatile State state = WAITING_FOR_HELLO_TO_IDENTIFY;

    public DiscordGateway(Session session, URI uri, String op2) throws IOException, WebSocketException {
        this.session = session;

        handlers.add(OpCodes.OP_0_DISPATCH, new InForwardingHandler(this));
        handlers.add(OpCodes.OP_1_HEARTBEAT, new InNoOpHandler(this)); // We may want to implement this
        handlers.add(OpCodes.OP_2_IDENTIFY, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_3_PRESENCE, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_4_VOICE_STATE, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_5_VOICE_PING, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_6_RESUME, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_7_RECONNECT, new InNoOpHandler(this)); //TODO
        handlers.add(OpCodes.OP_8_REQUEST_MEMBERS, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_9_INVALIDATE_SESSION, new InInvalidateSessionHandler(this));
        handlers.add(OpCodes.OP_10_HELLO, new InHelloHandler(this, op2));
        handlers.add(OpCodes.OP_11_HEARTBEAT_ACK, new InForwardingHandler(this)); // We may want to implement this
        handlers.add(OpCodes.OP_12_GUILD_SYNC, new InNoOpHandler(this));

        socket = new WebSocketFactory()
                .createSocket(uri)
                .addListener(this)
                .addHeader("Accept-Encoding", "gzip");

        socket.connect();
    }

    /* Thanks JDA folks for this method */
    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws UnsupportedEncodingException, DataFormatException
    {
        //Thanks to ShadowLordAlpha and Shredder121 for code and debugging.
        //Get the compressed message and inflate it
        ByteArrayOutputStream out = new ByteArrayOutputStream(binary.length * 2);
        try (InflaterOutputStream decompressor = new InflaterOutputStream(out))
        {
            decompressor.write(binary);
        }
        catch (IOException e)
        {
            throw (DataFormatException) new DataFormatException("Malformed").initCause(e);
        }

        // send the inflated message to the TextMessage method
        onTextMessage(websocket, out.toString("UTF-8"));
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

    public void forward(String message) {
        session.sendLocal(message);
    }

    public WebSocket getSocket() {
        return socket;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public Session getSession() {
        return session;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        log.info(this.state + " -> " + state);
        this.state = state;
    }

    public enum State {
        /**
         * We just started and are waiting for OP 10 so we can identify
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
         * We lost connection and are waiting to reconnect
         */
        WAITING_TO_RECONNECT

    }

}
