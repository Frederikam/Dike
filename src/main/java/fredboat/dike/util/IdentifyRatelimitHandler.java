package fredboat.dike.util;

import fredboat.dike.io.in.DiscordGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public class IdentifyRatelimitHandler {

    private static final Logger log = LoggerFactory.getLogger(IdentifyRatelimitHandler.class);
    private static final Duration BACKOFF = Duration.ofMillis(5050);

    private static final ConcurrentHashMap<Long, IdentifyRatelimitHandler> INSTANCES = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<WaitingShard> queue = new LinkedBlockingQueue<>();

    private IdentifyRatelimitHandler() {
        new IdentifyRatelimitThread().start();
    }

    public static IdentifyRatelimitHandler getInstance(long botId) {
        return INSTANCES.computeIfAbsent(botId, (s) -> new IdentifyRatelimitHandler());
    }

    public CountDownLatch acquire(DiscordGateway shard) throws InterruptedException {
        WaitingShard wshard = new WaitingShard(shard);
        if(!queue.add(wshard)) {
            throw new RuntimeException("Attempted to queue IDENTIFY, but the queue didn't change.");
        }

        // This latch is finished when we are ready to IDENTIFY
        wshard.greenlightLatch.await();

        return wshard.identifiedLatch;
    }

    private class IdentifyRatelimitThread extends Thread {

        IdentifyRatelimitThread() {
            setName("IdentifyRatelimitThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    handle();
                } catch (InterruptedException e) {
                    log.error("Got interrupted while handling identify ratelimits. Not good!", e);
                    return;
                } catch (Exception e) {
                    log.error("Exception while handling identify ratelimits! Continuing...", e);
                }
            }
        }

        void handle() throws InterruptedException {
            WaitingShard shard = queue.take();
            shard.greenlightLatch.countDown(); // Indicate that it is time to identify
            boolean reached = shard.identifiedLatch.await(BACKOFF.toMillis(), TimeUnit.MILLISECONDS);

            if (!reached) {
                log.warn("Timed out while waiting for {} to begin identifying. It likely failed. Moving on...", shard.shard);
            }

            sleep(BACKOFF.toMillis()); // Wait for the ratelimit duration
        }
    }

    private class WaitingShard {

        final DiscordGateway shard;
        /**
         * Counted down by the shard after it has finished identifying (or failed to do so), triggering a 5s ratelimit backoff
         */
        final CountDownLatch identifiedLatch = new CountDownLatch(1);

        /**
         * Counted down when the shard should identify
         */
        final CountDownLatch greenlightLatch = new CountDownLatch(1);

        WaitingShard(DiscordGateway shard) {
            this.shard = shard;
        }
    }

}
