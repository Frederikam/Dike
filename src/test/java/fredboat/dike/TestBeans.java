package fredboat.dike;

import fredboat.dike.session.cache.CacheTestEventListener;
import fredboat.dike.io.out.LocalGateway;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.utils.SessionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@TestConfiguration
public class TestBeans {

    @Bean
    Game game() {
        String build = System.getProperty("BUILD_NUMBER");
        return Game.playing(build != null ? "build # " + build : "unknown build");
    }

    @Bean
    @Autowired
    public JDA testBot(TestConfig config, Game game, CacheTestEventListener eventListener,
                       SessionController controller) throws LoginException, InterruptedException {
        LocalGateway localGateway = new LocalGateway(config);
        localGateway.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                localGateway.stop();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));

        return new JDABuilder(AccountType.BOT)
                .setToken(config.testBotToken())
                .useSharding(0, 1)
                .setGame(game)
                .addEventListener(eventListener)
                .setSessionController(controller)
                .buildBlocking();
    }

    @Bean
    public JDA userBot(TestConfig config, Game game) throws LoginException, InterruptedException {
        return new JDABuilder(AccountType.CLIENT)
                .setToken(config.testUserToken())
                .setGame(game)
                .buildBlocking();
    }

}
