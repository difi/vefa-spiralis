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

        System.setProperty(SpiralisConfigProperty.SPIRALIS_HOME, "/tmp");

        Path path = SpiralisHomeDirectory.locateSpiralisHomeDir();
        assertEquals(path, Paths.get("/tmp"));

    }

}