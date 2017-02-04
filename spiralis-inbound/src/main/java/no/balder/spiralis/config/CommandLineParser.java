package no.balder.spiralis.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 12.31
 */
public class CommandLineParser {

    private final OptionParser optionParser;
    private final OptionSpec<String> directoryOption;
    private OptionSet optionSet;

    public CommandLineParser() {

        optionParser = new OptionParser();
        directoryOption = optionParser.accepts("directory", "Directory to scan for input").withRequiredArg().describedAs("directory").required();

    }

    /**
     * Prints help 
     * @param outputStream
     */
    public void printHelp(OutputStream outputStream) {

        try {
            optionParser.printHelpOn(outputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to print help on supplied outputstream.",e);
        }
    }

    /**
     * Parses the command line options.
     * @param args
     * @return
     * @throws IllegalArgumentException
     */
    public OptionSet parse(String[] args) throws IllegalArgumentException {

        try {
            optionSet = optionParser.parse(args);
            return optionSet;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(),e);
        }
    }


    String getDirectory(){
        checkParsedState();

        return directoryOption.value(optionSet);
    }

    private void checkParsedState() {
        if (optionSet == null) {
            throw new IllegalStateException("Must parse arguments first");
        }
    }

    /**
     * Creates a TypeSafe Config object in which all pertinent command line options have been transferred.
     * @return an instance of {@link Config} holding command line parameters
     */
    public Config getConfig() {
        Map<String, String> m = new HashMap<>();

        m.put("spiralis.inbound.directory", getDirectory());

        Config config = ConfigFactory.parseMap(m, "Command line options");
        
        return config;
    }
}
