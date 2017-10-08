/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.DiscordGateway;

import java.io.IOException;

public abstract class IncomingHandler {

    final DiscordGateway discordGateway;

    public IncomingHandler(DiscordGateway discordGateway) {
        this.discordGateway = discordGateway;
    }

    public DiscordGateway getDiscordGateway() {
        return discordGateway;
    }

    public abstract void handle(String message) throws IOException;

}
