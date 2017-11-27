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
    private final BlockingQueue<DiscordGateway> reconnectQueue = new LinkedBlockingQueue<>();
    private volatile Thread reconnectThread;

    IdentifyQueue(long botId) {
        if (!instanceMap.containsKey(botId)) {
            instanceMap.put(botId, this);
        }
    }

    void append(DiscordGateway discordGateway) {
        reconnectQueue.add(discordGateway);
        if (reconnectThread == null) {
            reconnectThread = new ReconnectThread();
        }
    }

    private final class ReconnectThread extends Thread {

        private ReconnectThread() {
            setName("ReconnectThread");
            start();
        }

        @Override
        public final void run() {
            boolean isFirst = true;
            while (!reconnectQueue.isEmpty()) {
                try {
                    DiscordGateway gateway = reconnectQueue.poll();
                    if (isFirst) {
                        IdentifyRatelimitHandler.INSTANCE.acquire(gateway.getSession().getIdentifier().getUser());
                        gateway.setState(WAITING_FOR_HELLO_TO_IDENTIFY);
                        isFirst = false;
                    }
                    Log.info("Connecting gateway " + gateway.getSession().getIdentifier().getShardId());
                    gateway.connectSocket();
                    if (!reconnectQueue.isEmpty()) {
                        //TODO: Ensure shard has identified successfully before moving on to the next one
                        Thread.sleep(5_500); //Reconnect ratelimit
                    }
                } catch (Exception e) {
                    log.error("Encountered exception while connecting to discord", e);
                }

            }
            reconnectThread = null;
            if (!reconnectQueue.isEmpty()) {
                reconnectThread = new ReconnectThread();
            }
        }

    }

    static Map<Long, IdentifyQueue> getInstanceMap() {
        return instanceMap;
    }
}
