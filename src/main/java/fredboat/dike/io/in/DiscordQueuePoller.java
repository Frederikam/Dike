/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in;

import fredboat.dike.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * This queue is for messages that need to reach Discord, eventually, even if we reconnect.
 */
public class DiscordQueuePoller extends Thread {

    private final Session session;
    private final LinkedBlockingQueue<String> queue;
    private static final Logger log = LoggerFactory.getLogger(DiscordQueuePoller.class);
    private boolean shutdown = false;

    public DiscordQueuePoller(Session session, LinkedBlockingQueue<String> queue) {
        this.session = session;
        this.queue = queue;

        setName("DiscordQueuePoller " + session.getIdentifier().toStringShort());
    }

    @Override
    public void run() {
        DiscordGateway gateway = session.getDiscordGateway();
        MDC.put("shard", session.getIdentifier().toStringShort());

        try {
            //noinspection InfiniteLoopStatement
            while (!shutdown) {
                String string = queue.take();
                // Make sure the connection is not locked
                if (gateway.isLocked()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                log.info("Sent: " + string);
                gateway.sendAsync(string, false);
            }
        } catch (Exception e) {
            log.error("Caught exception in DiscordQueuePoller - NOT GOOD", e);
        }
    }

    public void shutdown() {
        shutdown = true;
    }
}
