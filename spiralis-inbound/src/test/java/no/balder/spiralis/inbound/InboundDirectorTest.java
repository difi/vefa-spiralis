package no.balder.spiralis.inbound;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import no.balder.spiralis.config.SpiralisConfigProperty;
import no.balder.spiralis.config.SpiralisInboundTestModuleFactory;
import no.balder.spiralis.payload.AzurePayloadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 19.18
 */
@Guice(moduleFactory = SpiralisInboundTestModuleFactory.class)
public class InboundDirectorTest {

    public static final String FILE_MATCH_GLOB = "glob:**-doc.xml";

    public static final Logger LOGGER = LoggerFactory.getLogger(InboundDirectorTest.class);

    @Inject
    Config config;

    @Test
    public void testStart() throws Exception {

        final Path dirPath = Paths.get("/var/peppol/IN");

        final String azureConnectionString = config.getString(SpiralisConfigProperty.SPIRALIS_AZURE_CONNECT);


        final InboundDirector inboundDirector = new InboundDirector(dirPath, FILE_MATCH_GLOB, new AzurePayloadStore(azureConnectionString));

        inboundDirector.start();

        InboundDirector.Statistics statistics;

        long start = System.currentTimeMillis();

        do {
            Thread.sleep(1000);
            statistics = inboundDirector.getProcessingStatistics();

            LOGGER.debug("Process statistics: " + statistics);
        } while (statistics.getProcessed() < 1414);

    }

}