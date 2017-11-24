package fredboat.dike.io.out.handle;

import com.jsoniter.JsonIterator;
import fredboat.dike.io.out.LocalGateway;
import fredboat.dike.session.Session;
import fredboat.dike.session.cache.Cache;
import fredboat.dike.session.cache.Dispatch;
import fredboat.dike.session.cache.Guild;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OutRequestGuildMembersHandler extends OutgoingHandler {

    private static final Logger log = LoggerFactory.getLogger(OutRequestGuildMembersHandler.class);

    OutRequestGuildMembersHandler(LocalGateway localGateway) {
        super(localGateway);
    }

    @Override
    public void handle(WebSocket socket, String message) throws IOException {
        String gId = JsonIterator.deserialize(message).get("d").get("guildId").toString();

        Session session = localGateway.getSession(socket);
        Cache cache = session.getCache();
        Guild guild;

        // We should be fine to sync a local variable
        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (cache) {
            guild = cache.getGuild(Long.parseLong(gId));
        }

        if (guild == null) {
            log.warn("Received REQUEST_GUILD_MEMBERS (OP 8) from bot, but guild is not in cache!");
            return;
        }

        for (Dispatch dispatch : guild.provideChunks()) {
            session.sendDispatch(dispatch);
        }
    }

}
