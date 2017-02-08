package no.balder.spiralis.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;

import static no.balder.spiralis.config.SpiralisConfigProperty.SPIRALIS_HOME;
/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 13.10
 */
public class SpiralisConfigurationModule extends AbstractModule {

    public static final Logger LOGGER = LoggerFactory.getLogger(SpiralisConfigurationModule.class);

    private final Config commandLineConfig;


    public SpiralisConfigurationModule(Config commandLineConfig) {
        this.commandLineConfig = commandLineConfig;
    }

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    @Named(SPIRALIS_HOME)
    protected Path spiralisHomeFolder() {
        Path path = SpiralisHomeDirectory.locateSpiralisHomeDir();
        return path;
    }

    /**
     * Loads the external configuration file, i.e. the {@code $SPIRALIS_HOME/spiralis.conf}
     *
     * @param spiralisHome full path to the external config file
     * @return a typesafe config object
     */
    @Provides
    @Singleton
    @Named("external.config")
    protected Config loadExternalConfigFile(@Named(SPIRALIS_HOME) Path spiralisHome) {

        Path configPath = spiralisHome.resolve("spiralis.conf");
        LOGGER.info("External configuration file: " + configPath);
        if (!Files.exists(spiralisHome)) {
            LOGGER.warn(configPath + " does not exist");
        }
        Config config = ConfigFactory.parseFile(configPath.toFile());
        return config;
    }

    
    @Provides
    @Singleton
    protected Config loadAndMergeConfiguration(@Named("external.config") Config externalConfig) {
        Config defaultReferenceConfig = ConfigFactory.defaultReference();   // Loads the reference.conf from class path

        // Loads and merges configuration in priority order
        Config effectiveMergedconfig = ConfigFactory.systemProperties()     // System properties overrides everything
                .withFallback(commandLineConfig)
                .withFallback(externalConfig)                               // The external configuration file
                .withFallback(defaultReferenceConfig)                       // The reference.conf files on class path
                .withFallback((defaultReferenceConfig.getConfig("default")));   // Finally, set default fall back values

        final Config resolved = effectiveMergedconfig.resolve();    // Resolves and substitutes any variables
        return resolved;
    }
}
