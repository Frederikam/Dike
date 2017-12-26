package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.DiscordGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InReconnectHandler extends IncomingHandler {

    private static final Logger log = LoggerFactory.getLogger(InReconnectHandler.class);

    public InReconnectHandler(DiscordGateway discordGateway) {
        super(discordGateway);
    }

    @Override
    public void handle(String message) {
        log.warn("Received OP 7 RECONNECT from Discord! We are forced to reconnect to a new gateway server.");

        discordGateway.getSocket().sendClose(1000, "OP 7");
    }
}
