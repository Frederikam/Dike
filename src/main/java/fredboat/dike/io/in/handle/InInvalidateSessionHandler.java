/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.DiscordGateway;
import org.json.JSONObject;

import java.io.IOException;

public class InInvalidateSessionHandler extends IncomingHandler {

    public InInvalidateSessionHandler(DiscordGateway discordGateway) {
        super(discordGateway);
    }

    @Override
    public void handle(String message) throws IOException {
        discordGateway.getSession().invalidate(
                new JSONObject(message).getBoolean("d")
        );
    }
}
