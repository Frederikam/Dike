package fredboat.dike.io.in;

import com.neovisionaries.ws.client.WebSocket;
import fredboat.dike.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class WebsocketRateLimitHandler {

    private static final Logger log = LoggerFactory.getLogger(WebsocketRateLimitHandler.class);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static final int NORMAL_LIMIT = Const.WEBSOCKET_RATE_LIMIT - 4;
    private static final int PRIORITY_LIMIT = 3;

    private final ExecutorService asyncThreads = Executors.newCachedThreadPool();
    private final AtomicInteger normalRemaining = new AtomicInteger(NORMAL_LIMIT);
    private final AtomicInteger priorityRemaining = new AtomicInteger(PRIORITY_LIMIT);
    private volatile ScheduledFuture resetFuture = null;
    private volatile WebSocket currentWs;
    /**
     * Level 1 for normal limit exceeded
     * Level 2 for priority limit exceeded
     * Resets to 0
     */
    private volatile byte warningLevel = 0;

    WebsocketRateLimitHandler() {}

    /**
     * Must be invoked when the websocket (re)connects.
     *
     * @param webSocket the websocket currently connected
     */
    void reset(WebSocket webSocket) {
        currentWs = webSocket;
        if (resetFuture != null) resetFuture.cancel(true);

        resetFuture = SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            normalRemaining.set(NORMAL_LIMIT);
            priorityRemaining.set(PRIORITY_LIMIT);
            synchronized (this) { notifyAll(); }
        }, 0, 60, TimeUnit.SECONDS);
    }

    void sendAsync(String message, boolean priority) {
        asyncThreads.submit(() -> {
            try {
                sendSync(message, priority);
            } catch (Exception e) {
                log.error("Caught exception while sending message", e);
            }
        });
    }

    private void sendSync(String message, boolean priority) throws InterruptedException {
        if (normalRemaining.getAndDecrement() <= 0) {
            if (!priority) {
                if (warningLevel < 1) {
                    warningLevel = 1;
                    log.warn("Hit the websocket rate limit!");
                }
                synchronized (this) {this.wait();}
                sendSync(message, false);
                return;
            }

            // Priority will draw from the priority limit if needed
            if (priorityRemaining.getAndDecrement() <= 0) {
                if (warningLevel < 2) {
                    warningLevel = 2;
                    log.warn("Hit the PRIORITY websocket rate limit!" +
                            " The session may be dropped because we can't heartbeat.");
                }
                synchronized (this) {this.wait();}
                sendSync(message, true);
            } else {
                normalRemaining.decrementAndGet();
                currentWs.sendText(message);
            }
        } else {
            normalRemaining.decrementAndGet();
            currentWs.sendText(message);
        }
    }

    void shutdown() {
        if (resetFuture != null) resetFuture.cancel(true);
        asyncThreads.shutdown();
    }

}
