/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in;

import fredboat.dike.cache.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

public class DiscordQueuePoller extends Thread {

    private final Session session;
    private final LinkedBlockingQueue<String> queue;
    private static final Logger log = LoggerFactory.getLogger(DiscordQueuePoller.class);

    public DiscordQueuePoller(Session session, LinkedBlockingQueue<String> queue) {
        this.session = session;
        this.queue = queue;

        setName("DiscordQueuePoller " + session.getIdentifier().toStringShort());
    }

    @Override
    public void run() {
        DiscordGateway gateway = session.getDiscordGateway();

        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                String string = queue.take();
                // Make sure the connection is not locked
                if (gateway.isLocked()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                gateway.getSocket().sendText(string);
            }
        } catch (Exception e) {
            log.error("Caught exception in DiscordQueuePoller - NOT GOOD", e);
        }
    }
}
