package no.balder.spiralis;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.peppol.persistence.guice.OxalisDataSourceModule;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import no.balder.spiralis.guice.JmsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static java.util.Arrays.asList;

/**
 *
 */
public class App {

    public static final Logger log = LoggerFactory.getLogger(App.class);

    public static OptionSpec<Void> help;
    public static OptionSpec<URL> endPointUrlOption;
    public static OptionSpec<String> brokerUrlOption;
    public static OptionSpec<Integer> maxNumberOfMessagesOption;
    public static OptionSpec<Integer> paralellTransmissionTasks;

    public static String effectiveBrokerUrl = "tcp://localhost:61616?jms.prefetchPolicy.queuePrefetch=1";

    public static void main(String[] args) {

        // Parses the command line
        OptionParser optionParser = createOptionParser();
        OptionSet optionSet = optionParser.parse(args);

        try {
            if (optionSet.has(help)) {
                printHelp(optionParser);
                return;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to print to stdout");
        }

        if (optionSet.has(brokerUrlOption)) {
            effectiveBrokerUrl = optionSet.valueOf(brokerUrlOption);
        }

        log.info("Connecting to JMS queue at " + effectiveBrokerUrl);


        Injector injector = Guice.createInjector(
                // new RepositoryModule(),
                new OxalisDataSourceModule(),
                // bnew OxalisProductionConfigurationModule(),
                new JmsModule(effectiveBrokerUrl)
        );

        OutboundWorkflowBuilder outboundWorkflowBuilder = injector.getInstance(OutboundWorkflowBuilder.class);

        // Has the end point for transport been overridden?
        if (optionSet.has(endPointUrlOption)) {
            URL url = optionSet.valueOf(endPointUrlOption);
            log.warn("PEPPOL access point URL has been overridden: " + url.toString());
            log.warn("SMP lookup will NOT be performed");

            outboundWorkflowBuilder.transmissionEndPoint(url.toString());
        }


        log.info("Using " + optionSet.valueOf(paralellTransmissionTasks) + " paralell transport tasks");

        OutboundWorkflow outboundWorkflow = outboundWorkflowBuilder
                .trasmissionTaskCount(optionSet.valueOf(paralellTransmissionTasks)) // Number of paralell tasks
                .build();

        outboundWorkflow.start();

        waitAlmostForever(outboundWorkflow, optionSet.valueOf(maxNumberOfMessagesOption));


    }

    static void waitAlmostForever(OutboundWorkflow outboundWorkflow, Integer numberOfMessageToProcess) {
        try {
            long processed = 0;
            int attempts = 0;
            do {

                Thread.sleep(1000);
                processed = outboundWorkflow.getTransmissionTransaction().getProcessCount();
                log.debug("Waiting for TransmissionTasks. " + attempts + " sleep invocations so far. Transmitted messages: " + processed + "of max " + numberOfMessageToProcess);
                attempts++;

            } while (!Thread.interrupted() && processed < numberOfMessageToProcess);
        } catch (InterruptedException e) {
            log.info("Interrupted, shutting down");
            outboundWorkflow.stop();
            return;
        }
    }

    private static void printHelp(OptionParser optionParser) throws IOException {
        System.out.println("");
        System.out.println("Outbound workflow - sendes messages in paralell");
        System.out.println();
        optionParser.printHelpOn(System.out);
    }


    static OptionParser createOptionParser() {

        // Initializes the OptionParser using double brace initialization, beware!
        OptionParser optionParser = new OptionParser() {
            {

                endPointUrlOption = accepts("e", "End point URL (ignore SMP)").withRequiredArg().ofType(URL.class)
                        .describedAs("Ignore end point URL in SMP and use supplied URL for all documents");

                brokerUrlOption = accepts("b", "Apache MQ broker URL").withRequiredArg().ofType(String.class)
                        .describedAs("The URL of the Apache MQ JMS broker");

                maxNumberOfMessagesOption = accepts("m", "Maximum number of messages to process").withRequiredArg().ofType(Integer.class).defaultsTo(Integer.MAX_VALUE)
                        .describedAs("The number of messages to process before terminating");

                paralellTransmissionTasks = accepts("p", "Number of paralell transport tasks").withRequiredArg().ofType(Integer.class).defaultsTo(20)
                    .describedAs("Number of transport tasks to run in paralell");

                help = acceptsAll(asList("h", "?"), "show help").forHelp();
            }
        };

        return optionParser;
    }

}
