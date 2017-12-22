/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike;

import fredboat.dike.io.out.LocalGateway;
import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@Controller
public class Launcher {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        SpringApplication sa = new SpringApplication(Launcher.class);
        sa.setWebEnvironment(false);
        sa.run(args);
    }

    @Autowired
    Launcher(LocalGateway gateway) {
        gateway.start();
    }

    @Bean
    static Config config(ConfigurationProvider configurationProvider) {
        return configurationProvider.bind("dike", Config.class);
    }

    @Bean
    static LocalGateway localGateway(Config config) {
        return new LocalGateway(config);
    }

}
