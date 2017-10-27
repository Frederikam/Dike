package fredboat.dike.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ThreadSafe
public class IdentifyRatelimitHandler {

    private static final Logger log = LoggerFactory.getLogger(IdentifyRatelimitHandler.class);
    private static final Duration BACKOFF = Duration.ofMillis(5500); // Not 5s because we don't want to take any risks
    public static final IdentifyRatelimitHandler INSTANCE = new IdentifyRatelimitHandler();

    private ConcurrentHashMap<Long, AtomicLong> nextTimeAvailable = new ConcurrentHashMap<>();

    private IdentifyRatelimitHandler() {
    }

    public synchronized void acquire(long id) throws InterruptedException {
        AtomicLong l = nextTimeAvailable.computeIfAbsent(id, s -> new AtomicLong(0));

        long safeTime = l.updateAndGet(operand -> Math.max(
                System.currentTimeMillis() + BACKOFF.toMillis(),
                operand + BACKOFF.toMillis()));

        safeTime -= BACKOFF.toMillis(); // This is the time it will be safe to return
        long sleepTime = safeTime - System.currentTimeMillis();

        if (sleepTime > 0) {
            log.info("Waiting " + sleepTime + "ms before identifying");
            Thread.sleep(sleepTime);
        }
    }
}
