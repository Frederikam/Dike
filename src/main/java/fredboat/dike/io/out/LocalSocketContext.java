package fredboat.dike.io.out;

import fredboat.dike.session.Session;
import org.java_websocket.WebSocket;

import javax.annotation.Nullable;
import java.time.Instant;

/**
 * Contains information about our local socket which may or may not have a session
 */
public class LocalSocketContext {

    private final WebSocket webSocket;
    @Nullable
    private Session session = null;
    private Instant lastHeartbeat = Instant.now();
    private volatile int lastHeartbeatSeq = -1;

    LocalSocketContext(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    @Nullable
    public Session getSession() {
        return session;
    }

    public void setSession(@Nullable Session session) {
        this.session = session;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public int getLastHeartbeatSeq() {
        return lastHeartbeatSeq;
    }

    public void onHeartbeat(int seq) {
        this.lastHeartbeat = Instant.now();
        lastHeartbeatSeq = seq;
    }
}
