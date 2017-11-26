package fredboat.dike.session;

import fredboat.dike.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
public class UnusedSessionWatcher extends Thread {
    private static final Logger log = LoggerFactory.getLogger(UnusedSessionWatcher.class);

    UnusedSessionWatcher() {
        setName("Unused session watcher");
    }

    @Override
    public void run() {
        int interval = 10_000;
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                cleanupSessions();

                Thread.sleep(interval);
            } catch (Exception e) {
                log.error("Encountered exception while cleaning up unused sessions");
            }
        }
    }

    private void cleanupSessions() {
        SessionManager.INSTANCE.getSessions().forEach(
                (id, session) -> {
                    if (!session.isBotConnected()
                            && session.getTimeBotDisconnected().getEpochSecond()
                            - Instant.now().getEpochSecond() > Config.unusedTimeoutMinutes * 60) {
                        log.info("Destroying session for timing out: " + id);

                        session.destroy();
                    }
                }
        );

    }

}
