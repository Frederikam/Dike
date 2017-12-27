package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.DiscordGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class InHeartbeatAckHandler extends IncomingHandler {

    private static final Logger log = LoggerFactory.getLogger(InHeartbeatAckHandler.class);

    private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(10);
    private volatile long lastAckTime = System.currentTimeMillis();

    public InHeartbeatAckHandler(DiscordGateway discordGateway) {
        super(discordGateway);
    }

    @Override
    public void handle(String message) {
        lastAckTime = System.currentTimeMillis();
    }


    /**
     * The task responsible for disconnecting connections after not getting a heartbeat ack for a while
     */
    public Runnable getHeartbeatAckWatchdogTask = () -> {
        try {
            long delta = System.currentTimeMillis() - lastAckTime;

            if (delta > TIMEOUT) {
                log.warn("We haven't received a heartbeat ack in {}ms! Reconnecting...", delta);
                discordGateway.getSocket().sendClose(4000, "Heartbeat ack timed out");
            } else if (delta > TIMEOUT / 4) {
                log.warn("We haven't received a heartbeat ack in {}ms. " +
                        "If this continues for much longer we will disconnect.", delta);
            }
        } catch (RuntimeException e) {
            log.error("Error in heartbeat ack watchdog", e);
        }
    };

}
