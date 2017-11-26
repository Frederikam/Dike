package fredboat.dike;

import com.kaaz.configuration.ConfigurationOption;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
public class Config {

    @ConfigurationOption
    public static int port = 9999;

    @ConfigurationOption
    public static String host = "0.0.0.0";

    @ConfigurationOption
    public static int unusedTimeoutMinutes = 60;

}
