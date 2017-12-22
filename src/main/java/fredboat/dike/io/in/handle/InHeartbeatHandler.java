package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.DiscordGateway;

/**
 * Discord may send a heartbeat request which we will need to respond to
 */
public class InHeartbeatHandler extends IncomingHandler {
    public InHeartbeatHandler(DiscordGateway discordGateway) {
        super(discordGateway);
    }

    @Override
    public void handle(String message) {
        discordGateway.getHeartbeater().beat();
    }
}
