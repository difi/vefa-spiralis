package no.balder.spiralis.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import static com.google.inject.Guice.createInjector;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 16.11
 */
public class InjectorHelper {


    public static Injector getInstance(Config config) {

        return Guice.createInjector(
                new SpiralisConfigurationModule(config)
        );

    }
}
