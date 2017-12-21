/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.DiscordGateway;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InInvalidateSessionHandler extends IncomingHandler {

    private static final Logger log = LoggerFactory.getLogger(InInvalidateSessionHandler.class);

    private boolean willIdentify = false;

    public InInvalidateSessionHandler(DiscordGateway discordGateway) {
        super(discordGateway);
    }

    @Override
    public void handle(String message) {
        willIdentify = !new JSONObject(message).getBoolean("d");
        log.info("Received OP 9. May resume: " + !willIdentify);

        discordGateway.getSocket().disconnect(1000, "OP 9");
    }

    public boolean shouldIdentify() {
        if (willIdentify) {
            willIdentify = false;
            return true;
        }
        return false;
    }


}
