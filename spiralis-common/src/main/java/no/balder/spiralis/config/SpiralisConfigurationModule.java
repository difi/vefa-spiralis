package no.balder.spiralis.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 13.10
 */
public class SpiralisConfigurationModule extends AbstractModule {

    public static final Logger LOGGER = LoggerFactory.getLogger(SpiralisConfigurationModule.class);

    private final Config commandLineConfig;

    /**
     * Loads a complete, merged set of Config objects. If you do not have a Config object obtained by
     * parsing the command line, you may use {@link ConfigFactory#empty()}
     *
     * @param commandLineConfig required argument
     */
    public SpiralisConfigurationModule(Config commandLineConfig) {
        if (commandLineConfig == null) {
            throw new IllegalArgumentException("Command line configuration object required, you may use ConfigFactory.empty()");
        }
        this.commandLineConfig = commandLineConfig;
    }

    @Override
    protected void configure() {

        final Config config = loadAndbindConfigNames();

        bind(Config.class).toInstance(config);
    }


    private Config loadAndbindConfigNames() {
        Config mergedConfig = ConfigLoader.instanceWithCommandLineConfigParams(commandLineConfig);

        for (String propertyName : SpiralisConfigProperty.getPropertyNames()) {
            if (mergedConfig.hasPath(propertyName)) {
                final String value = mergedConfig.getString(propertyName);
                LOGGER.debug("Binding {} to value \"{}\"", propertyName, value);
                bind(String.class).annotatedWith(Names.named(propertyName)).toInstance(value);
            }
        }

        return mergedConfig;
    }


}
