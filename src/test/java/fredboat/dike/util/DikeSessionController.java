package fredboat.dike.util;

import fredboat.dike.Config;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.utils.SessionControllerAdapter;
import net.dv8tion.jda.core.utils.tuple.ImmutablePair;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DikeSessionController extends SessionControllerAdapter {

    @Autowired
    private Config config;

    @Override
    public void appendSession(SessionConnectNode node) {
        try {
            node.run(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeSession(SessionConnectNode node) {
        // Nop
    }

    @Override
    public String getGateway(JDA api) {
        return String.format("ws://%s:%d", config.host(), config.port());
    }

    @Override
    public Pair<String, Integer> getGatewayBot(JDA api) {
        Pair<String, Integer> pair = super.getGatewayBot(api);

        return new ImmutablePair<>(getGateway(api), pair.getRight());
    }

}
