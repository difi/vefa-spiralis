package no.balder.spiralis.config;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 13.41
 */
public class SpiralisHomeDirectoryTest {

    @Test
    public void testLocateSpiralisHomeDir() throws Exception {

        final String value = System.getProperty("java.io.tmpdir");
        System.setProperty(SpiralisConfigProperty.SPIRALIS_HOME, value);

        Path path = SpiralisHomeDirectory.locateSpiralisHomeDir();
        assertEquals(path, Paths.get(value));

    }

}