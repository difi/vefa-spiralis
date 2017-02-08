package no.balder.spiralis;

import com.google.inject.Injector;
import com.typesafe.config.Config;
import no.balder.spiralis.config.CommandLineParser;
import no.balder.spiralis.config.InjectorHelper;
import no.balder.spiralis.inbound.InboundDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spiralis inbound scans all inbound payloads and their associated metadata and performs the following tasks:
 * <ol>
 *     <li>Stores the payload into global persistent storage using a pluggable interface</li>
 *     <li>Creates meta data entries in the database</li>
 *     <li>Creates statistics entries in the database</li>
 * </ol>
 */
public class App {

    public static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {

        CommandLineParser commandLineParser = new CommandLineParser();

        if (args.length == 0) {
            commandLineParser.printHelp(System.err);
            return;
        }

        // Parse the command line and transfers the options into a new Config object
        Config config = parseCommandLine(commandLineParser, args);

        // Creates injector using Config object
        Injector injector = InjectorHelper.getInstance(config);

        // Extracts the main class and executes
        InboundDirector inboundDirector = injector.getInstance(InboundDirector.class);

        try {
            inboundDirector.startThreads();
        } catch (InterruptedException e) {
            LOGGER.debug("Processing interrupted");
        }
    }

    static Config parseCommandLine(CommandLineParser commandLineParser, String[] args) {

        commandLineParser.parse(args);

        // Transfers commandline options into a new TypeSafe Config object.
        Config config = commandLineParser.getConfig();

        return config;
    }



}
