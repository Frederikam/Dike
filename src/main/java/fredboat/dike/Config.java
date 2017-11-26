package fredboat.dike;

import com.kaaz.configuration.ConfigurationOption;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
public class Config {

    @ConfigurationOption
    public static int dike_port = 9999;

    @ConfigurationOption
    public static String dike_host = "0.0.0.0";

    @ConfigurationOption
    public static int unused_timeout_minutes = 60;

}
