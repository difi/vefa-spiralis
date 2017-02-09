package no.balder.spiralis.inbound;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import no.balder.spiralis.config.SpiralisConfigProperty;
import no.balder.spiralis.config.SpiralisInboundTestModuleFactory;
import no.balder.spiralis.jdbc.SpiralisTaskPersister;
import no.balder.spiralis.payload.AzurePayloadStore;
import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.testutil.DummyFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 19.18
 */
@Guice(moduleFactory = SpiralisInboundTestModuleFactory.class)
public class InboundDirectorTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(InboundDirectorTest.class);

    @Inject
    Config config;

    @Inject
    SpiralisTaskPersister spiralisTaskPersister;

    @Test
    public void testStart() throws Exception {

        // final Path inboundDummyFiles = DummyFiles.createInboundDummyFilesInRootWithSubdirs();
        final Path inboundDirPath = Paths.get("/var/peppol/IN");
        final Path archiveDirPath = Paths.get("/var/peppol/ARCHIVE");

        final String azureConnectionString = config.getString(SpiralisConfigProperty.SPIRALIS_AZURE_CONNECT);
        final AzurePayloadStore payloadStore = new AzurePayloadStore(azureConnectionString);

        final InboundDirector inboundDirector = new InboundDirector(inboundDirPath, archiveDirPath,
                "glob:**" + WellKnownFileTypeSuffix.PAYLOAD.getSuffix(),
                payloadStore, spiralisTaskPersister);

        inboundDirector.startThreads();

        InboundDirector.Statistics statistics;

        long start = System.nanoTime();

        do {
            Thread.sleep(1000);
            statistics = inboundDirector.getProcessingStatistics();

            LOGGER.debug("Process statistics: " + statistics);
        } while (statistics.getProcessed() < 1414);

        long elapsedMs = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        System.out.println("Processed " + statistics.getProcessed() + " in " + elapsedMs);
    }
}