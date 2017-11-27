package fredboat.dike.io.in;

import com.oracle.tools.packager.Log;
import fredboat.dike.util.IdentifyRatelimitHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static fredboat.dike.io.in.DiscordGateway.State.WAITING_FOR_HELLO_TO_IDENTIFY;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
class IdentifyQueue {
    private static final Logger log = LoggerFactory.getLogger(IdentifyQueue.class);
    private static final Map<Long, IdentifyQueue> instanceMap = new ConcurrentHashMap<>();
    private final BlockingQueue<DiscordGateway> identifyQueue = new LinkedBlockingQueue<>();
    private volatile Thread identifyThread;

    void append(DiscordGateway discordGateway) {
        identifyQueue.add(discordGateway);
        if (identifyThread == null) {
            identifyThread = new ReconnectThread();
        }
    }

    private final class ReconnectThread extends Thread {

        private ReconnectThread() {
            setName("IdentifyThread");
            start();
        }

        @Override
        public final void run() {
            while (!identifyQueue.isEmpty()) {
                try {
                    DiscordGateway gateway = identifyQueue.poll();
                    Log.info("Connecting gateway " + gateway.getSession().getIdentifier().getShardId());
                    gateway.connectSocket();
                    if (!identifyQueue.isEmpty()) {
                        //TODO: Ensure shard has identified successfully before moving on to the next one
                        Thread.sleep(5_500); //Reconnect ratelimit
                    }
                } catch (Exception e) {
                    log.error("Encountered exception while connecting to discord", e);
                }

            }
            identifyThread = null;
        }

    }

    static IdentifyQueue getIdentifyQueue(long botId) {
        return instanceMap.computeIfAbsent(botId, k -> new IdentifyQueue());
    }
}
