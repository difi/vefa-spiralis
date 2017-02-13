package no.balder.spiralis.config;

import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;

import static no.balder.spiralis.config.SpiralisConfigProperty.SPIRALIS_HOME;

/**
 * @author steinar
 *         Date: 13.02.2017
 *         Time: 16.35
 */
public class ConfigLoader {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);


    /**
     * @param commandLineConfig
     * @return
     * @see #loadAndMergeConfiguration(Config, Config)
     */
    static Config instanceWithCommandLineConfigParams(Config commandLineConfig) {

        // Locates the Spiralis home folder
        final Path spiralisHomeFolder = spiralisHomeFolder();

        // Loads the external config file from the Spiralis home folder
        final Config externalConfig = loadExternalConfigFile(spiralisHomeFolder);

        // Merges the external config file with configuration parameteres given on the command line and transferred
        // into a Config object.
        final Config mergedConfig = loadAndMergeConfiguration(externalConfig, commandLineConfig);

        return mergedConfig;
    }

    /**
     * Provides a Configuration object without supplying anything using command line options.
     * @return a complete Config object
     */
    static Config instanceWithoutCommandLineConfigParams() {

        return instanceWithCommandLineConfigParams(ConfigFactory.empty());
    }


    /**
     * Locates the $SPIRALIS_HOME
     *
     * @return
     */
    static protected Path spiralisHomeFolder() {
        Path path = SpiralisHomeDirectory.locateSpiralisHomeDir();
        LOGGER.debug("Using " + path + " as Spiralis home folder");
        return path;
    }

    /**
     * Provides the complete configuration by merging Config objects in this order and presedence:
     * <ol>
     * <li>Java runtime system properties</li>
     * <li>Properties given as options on the command line</li>
     * <li>The external config file from "spiralis.home"</li>
     * <li>The reference.conf files found in the class path</li>
     * <li>The "default" Config object, which may be specified in the above Config objects</li>
     * </ol>
     *
     * @param externalConfig Config object created from external .config file found in "spiralis.home" directory
     * @param commandLineConfig Config object created from command line parameteres.
     * @return completely merged Config object
     */
    @Provides
    @Singleton
    static protected Config loadAndMergeConfiguration(Config externalConfig, Config commandLineConfig) {

        ConfigFactory.invalidateCaches();
        Config defaultReferenceConfig = ConfigFactory.defaultReference();   // Loads the reference.conf from class path

        // Loads and merges configuration in priority order
        final Config systemProperties = ConfigFactory.systemProperties();
        Config effectiveMergedconfig = systemProperties     // System properties overrides everything
                .withFallback(commandLineConfig)
                .withFallback(externalConfig)                               // The external configuration file
                .withFallback(defaultReferenceConfig)                       // The reference.conf files on class path
                .withFallback((defaultReferenceConfig.getConfig("default")));   // Finally, set default fall back values

        LOGGER.debug("Merged config before resolve; spiralis.home={}", effectiveMergedconfig.getString("spiralis.home"));
        final Config resolved = effectiveMergedconfig.resolve();    // Resolves and substitutes any variables

        return resolved;
    }


    static protected Config loadExternalConfigFile(Path spiralisHome) {

        Path configPath = spiralisHome.resolve("spiralis.conf");
        LOGGER.info("External configuration file: " + configPath);
        if (!Files.exists(spiralisHome)) {
            LOGGER.warn(configPath + " does not exist");
        }
        Config config = ConfigFactory.parseFile(configPath.toFile());
        return config;
    }
}
