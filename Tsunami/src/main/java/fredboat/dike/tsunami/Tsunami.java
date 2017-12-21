package fredboat.dike.tsunami;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class Tsunami {

    private static final Logger log = LoggerFactory.getLogger(Tsunami.class);

    public static void main(String[] args) throws LoginException, RateLimitedException, InterruptedException {
        Config config = new Config();

        log.info("Starting Tsunami with " + config.getShardCount() + " shards");

        for (int i = 0; i < config.getShardCount(); i++) {
            new JDABuilder(AccountType.BOT)
                    .setToken(config.getToken())
                    .setGatewayProviderFactory(jda -> () -> "ws://localhost:9999")
                    .useSharding(i, config.getShardCount())
                    .addEventListener(new VerboseEventListener())
                    .buildAsync();
        }
    }

}
