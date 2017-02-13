package no.balder.spiralis.config;

import com.typesafe.config.Config;
import joptsimple.OptionSet;
import no.balder.spiralis.config.CommandLineParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 12.36
 */
public class CommandLineParserTest {

    private CommandLineParser commandLineParser;

    @BeforeMethod
    public void setUp() throws Exception {
        commandLineParser = new CommandLineParser();

    }

    @Test
    public void testPrintHelp() throws Exception {

        commandLineParser.printHelp(System.out);
    }


    @Test
    public void testParse() throws Exception {

        OptionSet options = commandLineParser.parse(new String[]{"-d", "/tmp", "-archive","/tmp/archive"});

        assertTrue(options.has("directory"));
        assertTrue(options.hasArgument("directory"));
        assertEquals(options.valueOf("directory"), "/tmp");

    }

    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void throwsIfMissingRequired() throws Exception {

        OptionSet options = commandLineParser.parse(new String[]{});
        
        options = commandLineParser.parse(new String[]{});
        assertTrue(options.has("directory") == false);
    }

    @Test
    public void getConfig() throws Exception {
        OptionSet options = commandLineParser.parse(new String[]{"-d", "/tmp/in", "-a", "/tmp/archive"});

        Config config = commandLineParser.getConfig();
        assertNotNull(config);
        assertTrue(config.hasPath("spiralis.inbound.directory"));
        assertEquals(config.getString("spiralis.inbound.directory"), "/tmp/in");
    }
}