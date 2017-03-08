package no.balder.spiralis.inbound;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import no.balder.spiralis.config.SpiralisConfigProperty;
import no.balder.spiralis.config.SpiralisInboundTestModuleFactory;
import no.balder.spiralis.jdbc.SpiralisTaskPersister;
import no.balder.spiralis.payload.AzurePayloadStore;
import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.testutil.DummyFiles;
import no.balder.spiralis.tool.gson.GsonHelper;
import no.balder.spiralis.transport.ReceptionMetaData;
import no.difi.oxalis.api.inbound.InboundMetadata;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 20.34
 */
@Guice(moduleFactory = SpiralisInboundTestModuleFactory.class)
public class ProcessActivityTest {

    @Inject
    Config config;
    @Inject
    SpiralisTaskPersister spiralisTaskPersister;
    @Inject
    DataSource dataSource;
    private Path rootPath;

    /**
     * Creates a directory holding a set of files received.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        rootPath = DummyFiles.createInboundDummyFilesInRootWithOptionalSubdirs();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        assertNotNull(rootPath);
        DummyFiles.removeAll(rootPath);
    }


    @Test
    public void processSingleTask() throws Exception {

        assertNotNull(rootPath);

        // Creates directory with sample input files
        final List<Path> paths = DummyFiles.locateJsonMetaData(rootPath);

        final List<Path> jsonMetaData = DummyFiles.locateJsonMetaData(rootPath);
        final InboundMetadata inboundMetadata = GsonHelper.fromJson(jsonMetaData.get(0));

        // Creates the SpiralisReceptionTask based upon the contents in the sample dummy files
        final SpiralisReceptionTask spiralisReceptionTask = SpiralisTaskFactory.insepctInbound(paths.get(0));

        final BlockingQueue<SpiralisReceptionTask> blockingQueue = new LinkedBlockingQueue<>();

        final String azureConnectionString = config.getString(SpiralisConfigProperty.SPIRALIS_AZURE_CONNECT);
        final AzurePayloadStore payloadStore = new AzurePayloadStore(azureConnectionString);

        final Path archive = Files.createTempDirectory("ARCHIVE");

        final ProcessActivity processActivity = new ProcessActivity(blockingQueue, payloadStore, spiralisTaskPersister, rootPath, archive);
        processActivity.startThreads();

        blockingQueue.put(spiralisReceptionTask);   // Insert the test data on the queue

        // Waits for the task to be processed....
        long start = System.currentTimeMillis();
        do {
            Thread.sleep(1000);
        } while (processActivity.getProcessedCounter() == 0 && System.currentTimeMillis() < start + 10000);


        // Verifies that we have processed a single task
        assertEquals(processActivity.getProcessedCounter().longValue(), 1L);

        // The input files should have been moved away
        assertFalse(Files.exists(spiralisReceptionTask.getPayloadPath()), "The original payload is still there");  // The entry should have been moved away now
        assertFalse(Files.exists(spiralisReceptionTask.getRemEvidencePath()), "The original REM evidence file is still there");  // The entry should have been moved away now

        List<Path> archivedPaths = new ArrayList<>();
        Files.walkFileTree(archive, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                archivedPaths.add(file);
                return FileVisitResult.CONTINUE;
            }
        });

        // And there should be two files in the archive
        assertEquals(archivedPaths.size(), 4);

        // Verifies the contents of the database ....
        assertNotNull(dataSource);

        assertNotNull(spiralisReceptionTask.getReceptionId());

        final Optional<ReceptionMetaData> byReceptionId = spiralisTaskPersister.findByReceptionId(spiralisReceptionTask.getReceptionId());
        assertTrue(byReceptionId.isPresent());
        final ReceptionMetaData rm = byReceptionId.get();
        assertNotNull(rm.getAccountId());
        assertNotNull(rm.getDirection());
        assertNotNull(rm.getReceived());
        assertNull(rm.getDelivered());
        assertNotNull(rm.getSender());
        assertNotNull(rm.getReceiver());
        assertNotNull(rm.getChannel());
        assertNotNull(rm.getReceptionId());
        assertNotNull(rm.getTransmissionId());
        assertNotNull(rm.getInstanceId(), inboundMetadata.getHeader().getIdentifier().getValue());
        assertNotNull(rm.getDocumentTypeId());
        assertNotNull(rm.getProcessTypeId());
        assertNotNull(rm.getApName());
        assertTrue(rm.getApName().contains("APP_"), "Seems the access point identifier was not extraced from the Certificate:" + rm.getApName());
        assertNotNull(rm.getPayloadUrl());
        assertNotNull(rm.getEvidenceUrl());

        // TODO: Verify contents of Blob store
        
    }

    @Test
    public void missingRemEvidenceHandled() throws Exception {

        final List<Path> paths = DummyFiles.locateJsonMetaData(rootPath);
        final List<Path> jsonMetaData = DummyFiles.locateJsonMetaData(rootPath);
        final Path remPath = DummyFiles.locateFiles(rootPath, WellKnownFileTypeSuffix.REM_EVIDENCE).get(0);
        assertNotNull(remPath);
        Files.delete(remPath);  // Removes the REM evidence file to trigger the handling

        // Creates the SpiralisReceptionTask based upon the contents in the sample dummy files
        final SpiralisReceptionTask spiralisReceptionTask = SpiralisTaskFactory.insepctInbound(paths.get(0));

        final BlockingQueue<SpiralisReceptionTask> blockingQueue = new LinkedBlockingQueue<>();

        final String azureConnectionString = config.getString(SpiralisConfigProperty.SPIRALIS_AZURE_CONNECT);
        final AzurePayloadStore payloadStore = new AzurePayloadStore(azureConnectionString);

        final Path archive = Files.createTempDirectory("ARCHIVE");

        final ProcessActivity processActivity = new ProcessActivity(blockingQueue, payloadStore, spiralisTaskPersister, rootPath, archive);
        processActivity.startThreads();

        blockingQueue.put(spiralisReceptionTask);   // Insert the test data on the queue

        // Waits for the task to be processed....
        long start = System.currentTimeMillis();
        do {
            Thread.sleep(1000);
        } while (processActivity.getProcessedCounter() == 0 && System.currentTimeMillis() < start + 10000);


    }
}