/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in;

import fredboat.dike.util.OpCodes;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Heartbeater extends Thread {

    private static final Logger log = LoggerFactory.getLogger(Heartbeater.class);
    private final DiscordGateway gateway;
    private final int interval;
    private int sequence = 0;

    //TODO: Kill this thread after session dies
    public Heartbeater(DiscordGateway gateway, int interval) {
        this.gateway = gateway;
        this.interval = interval;

        setName("Heartbeater " + gateway.getSession().getIdentifier().toStringShort());
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                // Make sure we're not locked
                while (gateway.isLocked()) {
                    // Gateway is locked. We will need to wait before we are allowed to send heartbeats
                    Thread.sleep(1000);
                }

                JSONObject json = new JSONObject();
                json.put("op", OpCodes.OP_1_HEARTBEAT);
                json.put("d", sequence);
                sequence++;

                gateway.getSession().sendDiscord(json.toString());

                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.error("Heartbeat thread got interrupted!", e);
            }
        }
    }
}
