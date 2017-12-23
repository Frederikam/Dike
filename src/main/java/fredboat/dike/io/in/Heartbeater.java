/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in;

import fredboat.dike.util.OpCodes;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicInteger;

public class Heartbeater extends Thread {

    private static final Logger log = LoggerFactory.getLogger(Heartbeater.class);
    private final DiscordGateway gateway;
    private int interval;
    private final AtomicInteger sequence = new AtomicInteger();
    private volatile boolean enabled = false;
    private volatile boolean shutdown = false;

    Heartbeater(DiscordGateway gateway) {
        this.gateway = gateway;
        this.interval = 41250; // Discord may send a different value
    }

    @Override
    public void run() {
        setName("Heartbeater " + gateway.getSession().getIdentifier().toStringShort());
        MDC.put("shard", gateway.getSession().getIdentifier().toStringShort());
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                if (!enabled) this.wait();

                beat();

                Thread.sleep(interval);
            } catch (InterruptedException e) {
                if (!shutdown) // This will intentionally happen when shutting down
                    log.error("Heartbeat thread got interrupted. NOT GOOD!", e);
                return;
            }
        }
    }

    public void beat() {
        JSONObject json = new JSONObject();
        json.put("op", OpCodes.OP_1_HEARTBEAT);
        json.put("d", sequence.get());
        sequence.getAndIncrement();

        gateway.sendAsync(json.toString(), true);
    }

    void shutdown() {
        shutdown = true;
        interrupt();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            synchronized (this) {
                notify();
            }
        }

        if (enabled && getState() == State.NEW) this.start();
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
