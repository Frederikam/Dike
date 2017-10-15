/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.DiscordGateway;
import fredboat.dike.io.in.Heartbeater;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InHelloHandler extends IncomingHandler {

    private static final Logger log = LoggerFactory.getLogger(InHelloHandler.class);

    private final String op2;
    @SuppressWarnings("FieldCanBeLocal")
    private Heartbeater heartbeater = null;

    public InHelloHandler(DiscordGateway discordGateway, String op2) {
        super(discordGateway);
        this.op2 = op2;
    }

    @Override
    public void handle(String message) throws IOException {
        log.info("Received HELLO OP 10, sending IDENTIFY OP 2");
        discordGateway.getSocket().sendText(op2);
        discordGateway.setLocked(false);

        // Start heartbeating
        int interval = new JSONObject(message).getJSONObject("d").getInt("heartbeat_interval");
        heartbeater = new Heartbeater(discordGateway, interval);
        heartbeater.start();
        discordGateway.setState(DiscordGateway.State.IDENTIFYING);
    }
}
