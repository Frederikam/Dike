package fredboat.dike.tsunami;

import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerboseEventListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(VerboseEventListener.class);

    @Override
    public void onReady(ReadyEvent event) {
        log.info(event.getJDA() + " readied");
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        log.info(event.getJDA() + " reconnected");
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        log.info(event.getJDA() + " disconnected");
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        log.info(event.getJDA() + " user {} said {}", event.getMember(), event.getMessage().getRawContent());
    }
}
