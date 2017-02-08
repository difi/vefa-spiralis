package no.balder.spiralis.config;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static no.balder.spiralis.config.SpiralisConfigProperty.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 13.39
 */
public class SpiralisConfigurationModuleTest {

    

    @Inject
    Injector injector;

    private void createSampleConfigFile(Path directory, String name) throws IOException {

        final Path configPath = directory.resolve(name);

        String[] lines = {
                "spiralis { foo: bar }",
                "include \"jdbc.conf\""
        };

        Files.write(configPath, Arrays.asList(lines), Charset.defaultCharset());

        Files.write(directory.resolve("jdbc.conf"), "spiralis { jdbc.driver: foo}".getBytes());
    }

    @BeforeMethod
    public void setUp() throws Exception {

        final String tmpDirName = System.getProperty("java.io.tmpdir");
        System.setProperty(SPIRALIS_HOME, tmpDirName);

        Config dummyCommandLineParamsConfig = ConfigFactory.load("dummy");

        createSampleConfigFile(Paths.get(tmpDirName), "spiralis.conf");
        SpiralisConfigurationModule spiralisConfigurationModule = new SpiralisConfigurationModule(dummyCommandLineParamsConfig);
        Injector injector = Guice.createInjector(spiralisConfigurationModule);
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
    public void verifyIncludeStatemet() throws Exception {

        final Config config = injector.getInstance(Config.class);
        assertTrue(config.hasPath("spiralis.foo"));


    }

    /**
     * Verifies that we have access to the Config named "external.config"
     * @throws Exception
     */
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