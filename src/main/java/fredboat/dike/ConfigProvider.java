package fredboat.dike;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.Arrays;

@Configuration
public class ConfigProvider {

    @Value("${configFilesPath:.}")
    private String filesPath; // Run with -DconfigFilesPath=<configFilesPath> parameter to override

    @Bean
    public ConfigurationProvider configurationProvider() {
        // Specify which files to load. Configuration from both files will be merged.
        //noinspection ArraysAsListWithZeroOrOneArgument
        ConfigFilesProvider configFilesProvider = () -> Arrays.asList(Paths.get("dike.yaml"));

        // Use local files as configuration store
        ConfigurationSource source = new FilesConfigurationSource(configFilesProvider);

        // Use relative paths
        Environment environment = new ImmutableEnvironment(filesPath);

        // Reload configuration every 5 seconds
        //ReloadStrategy reloadStrategy = new PeriodicalReloadStrategy(5, TimeUnit.SECONDS);

        // Create provider
        return new ConfigurationProviderBuilder()
                .withConfigurationSource(source)
                //.withReloadStrategy(reloadStrategy)
                .withEnvironment(environment)
                .build();
    }
}