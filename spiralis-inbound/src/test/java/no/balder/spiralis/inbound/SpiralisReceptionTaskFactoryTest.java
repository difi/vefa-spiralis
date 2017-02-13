package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.PayloadPathUtil;
import no.balder.spiralis.testutil.DummyFiles;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 19.59
 */
public class SpiralisReceptionTaskFactoryTest {

    private Path rootPath;

    @BeforeMethod
    public void setUp() throws Exception {
        rootPath = DummyFiles.createInboundDummyFilesInRootWithSubdirs();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        DummyFiles.removeAll(rootPath);
    }

    @Test
    public void testInsepct() throws Exception {

        // Traverses the dummy files ..
        final List<Path> paths = DummyFiles.locatePayloadFilesIn(rootPath);

        // Creates the SpiralisReceptionTask based upon the contents in the sample dummy files
        final SpiralisReceptionTask spiralisReceptionTask = SpiralisTaskFactory.insepctInbound(paths.get(0));

        assertNotNull(spiralisReceptionTask);

        assertNotNull(spiralisReceptionTask.getHeader());
        assertNotNull(spiralisReceptionTask.getPayloadPath(), "Payload path not set in task");
        assertNotNull(spiralisReceptionTask.getSmimePath(),"S/MIME path not set in task");
        assertTrue(Files.exists(spiralisReceptionTask.getPayloadPath()),"Payload file " + spiralisReceptionTask.getPayloadPath() + " does not exist");
        assertTrue(Files.exists(spiralisReceptionTask.getSmimePath()), "S/MIME file " + spiralisReceptionTask.getSmimePath() + " does not exist");

        assertNotNull(spiralisReceptionTask.getHeader().getCreationTimestamp(), "Creation timestamp is null");
        assertNotNull(spiralisReceptionTask.getHeader().getDocumentType(), "Document Type id not found");
        assertNotNull(spiralisReceptionTask.getHeader().getIdentifier(), "Instance identifier not found in SBDH");
        assertNotNull(spiralisReceptionTask.getHeader().getProcess(), "Process missing from SBDH");
        assertNotNull(spiralisReceptionTask.getHeader().getReceiver(), "Receiver not obtained from SBDH");
        assertNotNull(spiralisReceptionTask.getHeader().getSender(), "Sender not obtained from SBDH");

        assertNotNull(spiralisReceptionTask.getTransmissionId(), "Seems we were not able to find the AS2 message-ID");
        assertNotNull(spiralisReceptionTask.getReceptionId(), "No ReceptionID assigned");
    }
    

    @Test
    public void testBaseFileName() throws Exception {

        final Path inboundDummyFilesInRootWithSubdirs = DummyFiles.createInboundDummyFilesInRootWithSubdirs();
        final List<Path> paths = DummyFiles.locatePayloadFilesIn(inboundDummyFilesInRootWithSubdirs);

        final Path path = paths.get(0);

        // Extracts the base name, i.e. the filename without the extension
        // Extracts the UUID part of the filename, i.e. the suffix is discarded
        final String s = PayloadPathUtil.fileNameBodyPart(path);
        assertEquals(s, DummyFiles.SAMPLE_UUID);
    }
}