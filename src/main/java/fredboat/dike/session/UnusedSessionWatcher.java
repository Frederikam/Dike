package fredboat.dike.session;

import fredboat.dike.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
public class UnusedSessionWatcher extends Thread {
    private static final Logger log = LoggerFactory.getLogger(UnusedSessionWatcher.class);
    private final ConcurrentHashMap<ShardIdentifier, Long> sessionsLastConnected = new ConcurrentHashMap<>();
    private final int unusedTimeoutMillis = Config.unused_timeout_minutes * 60 * 1000;
    private final int interval = 10_000; //Run every 10 seconds

    public UnusedSessionWatcher() {
        setName("Unused session watcher");
    }

    @Override
    public void run() {
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
                (k, v) -> {
                    if (!v.getLocalSocket().isOpen()) {
                        if (!sessionsLastConnected.containsKey(k)) {
                            sessionsLastConnected.put(k, System.currentTimeMillis());
                        }
                    } else {
                        sessionsLastConnected.remove(k);
                    }
                    if ((System.currentTimeMillis() - sessionsLastConnected.get(k)) > unusedTimeoutMillis) {
                        SessionManager.INSTANCE.killSession(k, v);
                    }
                }
        );

    }

}
