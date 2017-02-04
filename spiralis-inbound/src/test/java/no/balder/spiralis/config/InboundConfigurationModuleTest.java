package no.balder.spiralis.config;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.balder.spiralis.config.InboundConfigurationModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static no.balder.spiralis.config.SpiralisConfigProperty.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 13.39
 */
public class InboundConfigurationModuleTest {


    @Inject
    Injector injector;

    @BeforeMethod
    public void setUp() throws Exception {

        System.setProperty(SPIRALIS_HOME, System.getProperty("java.io.tmpdir"));
        
        Config dummy = ConfigFactory.load("dummy");

        InboundConfigurationModule inboundConfigurationModule = new InboundConfigurationModule(dummy);
        Injector injector = Guice.createInjector(inboundConfigurationModule);
        injector.injectMembers(this);

        assertNotNull(this.injector);
    }

    /**
     * The System property should be reflected in the Guice injectior.
     * @throws Exception
     */
    @Test
    public void testSpiralisHomeFolder() throws Exception {

        Path path = injector.getInstance(Key.get(Path.class, Names.named(SPIRALIS_HOME)));
        assertNotNull(path);
        assertEquals(path, Paths.get(System.getProperty("java.io.tmpdir")));
    }

    @Test
    public void testLoadExternalConfigFile() throws Exception {
        Config instance = injector.getInstance(Key.get(Config.class, Names.named("external.config")));
        assertNotNull(instance);
    }

    /**
     * Complete configuration in prioritized order should have been loaded.
     *
     * @throws Exception
     */
    @Test
    public void testLoadAndMergeConfiguration() throws Exception {

        Config config = injector.getInstance(Config.class);
        assertNotNull(config);

        // Verifies a property which ought to be included from System.properties.
        assertTrue(config.hasPath("user.home"));
    }

}