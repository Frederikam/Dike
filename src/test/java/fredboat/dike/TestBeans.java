package fredboat.dike;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.security.auth.login.LoginException;

@TestConfiguration
public class TestBeans {

    @Bean
    Game game() {
        String build = System.getProperty("BUILD_NUMBER");
        return Game.playing(build != null ? "build # " + build : "unknown build");
    }

    @Bean
    public JDA testBot(TestConfigImpl config, Game game) throws LoginException, InterruptedException {
        return new JDABuilder(AccountType.BOT)
                .setToken(config.testBotToken())
                .useSharding(0, 1)
                .setGame(game)
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
