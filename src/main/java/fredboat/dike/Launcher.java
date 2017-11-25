/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike;

import com.kaaz.configuration.ConfigurationBuilder;
import fredboat.dike.io.out.LocalGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Launcher {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        try {
            new ConfigurationBuilder(Config.class, new File("dike.cfg")).build(true);
        } catch (Exception e) {
            log.error("Encountered exception while initiating ConfigurationBuilder", e);
            System.exit(1);
        }
        new LocalGateway().start();
    }

}
