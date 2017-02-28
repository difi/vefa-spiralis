package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.ReceptionPathUtil;
import no.balder.spiralis.testutil.DummyFiles;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
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
        rootPath = DummyFiles.createInboundDummyFilesInRootWithOptionalSubdirs();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        DummyFiles.removeAll(rootPath);
    }

    @Test
    public void testInsepct() throws Exception {

        // Traverses the dummy files ..
        final List<Path> paths = DummyFiles.locateJsonMetaData(rootPath);
        // Creates the SpiralisReceptionTask based upon the contents in the sample dummy files
        final SpiralisReceptionTask spiralisReceptionTask = SpiralisTaskFactory.insepctInbound(paths.get(0));

        assertNotNull(spiralisReceptionTask);

        assertNotNull(spiralisReceptionTask.getInboundMetadata());
        assertNotNull(spiralisReceptionTask.getPayloadPath(), "Payload path not set in task");
        assertNotNull(spiralisReceptionTask.getRemEvidencePath(),"S/MIME path not set in task");
        assertTrue(Files.exists(spiralisReceptionTask.getPayloadPath()),"Payload file " + spiralisReceptionTask.getPayloadPath() + " does not exist");
        assertTrue(Files.exists(spiralisReceptionTask.getRemEvidencePath()), "S/MIME file " + spiralisReceptionTask.getRemEvidencePath() + " does not exist");

        assertNotNull(spiralisReceptionTask.getInboundMetadata().getTimestamp(), "Creation timestamp is null");
        assertNotNull(spiralisReceptionTask.getInboundMetadata().getHeader().getDocumentType(), "Document Type id not found");
        assertNotNull(spiralisReceptionTask.getInboundMetadata().getHeader().getIdentifier(), "Instance identifier not found in SBDH");
        assertNotNull(spiralisReceptionTask.getInboundMetadata().getHeader().getProcess(), "Process missing from SBDH");
        assertNotNull(spiralisReceptionTask.getInboundMetadata().getHeader().getReceiver(), "Receiver not obtained from SBDH");
        assertNotNull(spiralisReceptionTask.getInboundMetadata().getHeader().getSender(), "Sender not obtained from SBDH");

        assertNotNull(spiralisReceptionTask.getInboundMetadata().getTransmissionIdentifier(), "Seems we were not able to find the AS2 message-ID");
        assertNotNull(spiralisReceptionTask.getReceptionId(), "No ReceptionID assigned");
    }
    

    @Test
    public void testBaseFileName() throws Exception {

        final Path inboundDummyFilesInRootWithSubdirs = DummyFiles.createInboundDummyFilesInRootWithOptionalSubdirs();
        final List<Path> paths = DummyFiles.locatePayloadFilesIn(inboundDummyFilesInRootWithSubdirs);

        final Path path = paths.get(0);

        // Extracts the base name, i.e. the filename without the extension
        // Extracts the UUID part of the filename, i.e. the suffix is discarded
        final String s = ReceptionPathUtil.fileNameBodyPart(path);
        assertEquals(s, DummyFiles.SAMPLE_TRANSMISSION_ID);
    }
}