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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static org.testng.Assert.assertEquals;

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
    private Path inboundDir;
    private Path archiveDir;

    @BeforeMethod
    public void setUp() throws Exception {
        final Path rootDir = DummyFiles.createInboundDummyFilesInRootWithOptionalSubdirs("IN");
        inboundDir = rootDir.resolve("IN");
        archiveDir = rootDir.resolve("ARCHIVE");
        Files.createDirectories(archiveDir);
    }

    @Test(enabled = true)
    public void hafslundTest() throws Exception {

        // final Path inboundDummyFiles = DummyFiles.createInboundDummyFilesInRootWithOptionalSubdirs();

        final String azureConnectionString = config.getString(SpiralisConfigProperty.SPIRALIS_AZURE_CONNECT);
        final AzurePayloadStore payloadStore = new AzurePayloadStore(azureConnectionString);

        final InboundDirector inboundDirector = new InboundDirector(inboundDir, archiveDir,
                "glob:**" + WellKnownFileTypeSuffix.META_JSON.getSuffix(),
                payloadStore, spiralisTaskPersister);

        inboundDirector.startThreads();

        InboundDirector.Statistics statistics;

        long start = System.nanoTime();

        do {
            Thread.sleep(1000);
            statistics = inboundDirector.getProcessingStatistics();

            LOGGER.debug("Process statistics: " + statistics);
        } while (statistics.getProcessed() < 1);

        long elapsedMs = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        System.out.println("Processed " + statistics.getProcessed() + " in " + elapsedMs);


        assertEquals( inboundDir.toFile().listFiles().length,0);

        

    }
}