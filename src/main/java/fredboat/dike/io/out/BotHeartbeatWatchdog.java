package fredboat.dike.io.out;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * This task monitors the latest received heartbeats
 */
public class BotHeartbeatWatchdog implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BotHeartbeatWatchdog.class);
    private static final long serverBeatThreshold = 50000;  // Threshold at which we request a heartbeat
    private static final long disconnectThreshold = 100000; // Threshold at which we disconnect

    private final LocalGateway gateway;

    BotHeartbeatWatchdog(LocalGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void run() {
        gateway.getContexts().forEach(context -> {
            try {
                long msSinceHb = Instant.now().minusMillis(context.getLastHeartbeat().toEpochMilli()).toEpochMilli();

                if (msSinceHb > disconnectThreshold) {
                    log.warn("Bot hasn't heartbeated in {}ms! Closing the connection. Session: {}",
                            msSinceHb, context.getSession());

                    context.getWebSocket().close(1000, "Heartbeat timed out");
                } else if (msSinceHb > serverBeatThreshold) {
                    // Send a beat to request a response beat
                    log.warn("Bot hasn't heartbeated in {}ms. Requesting a heartbeat. Session: {}",
                            msSinceHb, context.getSession());

                    JSONObject json = new JSONObject()
                            .put("op", 1)
                            .put("d", context.getLastHeartbeatSeq());
                    context.getWebSocket().send(json.toString());
                }
            } catch (RuntimeException e) {
                log.error("Caught exception in BotHeartbeatWatchdog task", e);
            }
        });
    }

}
