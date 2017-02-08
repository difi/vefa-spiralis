package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.testutil.DummyFiles;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 19.59
 */
public class SpiralisTaskFactoryTest {

    private Path rootPath;

    @BeforeMethod
    public void setUp() throws Exception {
        rootPath = DummyFiles.createInboundDummyFiles();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        DummyFiles.removeAll(rootPath);
    }

    @Test
    public void testInsepct() throws Exception {

        // Traverses the dummy files ..
        final List<Path> paths = DummyFiles.locatePayloadFilesIn(rootPath);

        // Creates the SpiralisTask based upon the contents in the sample dummy files
        final SpiralisTask spiralisTask = SpiralisTaskFactory.insepctInbound(paths.get(0));

        assertNotNull(spiralisTask);

        assertNotNull(spiralisTask.getHeader());
        assertNotNull(spiralisTask.getPayloadPath(), "Payload path not set in task");
        assertNotNull(spiralisTask.getSmimePath(),"S/MIME path not set in task");
        assertTrue(Files.exists(spiralisTask.getPayloadPath()),"Payload file " + spiralisTask.getPayloadPath() + " does not exist");
        assertTrue(Files.exists(spiralisTask.getSmimePath()), "S/MIME file " + spiralisTask.getSmimePath() + " does not exist");

        assertNotNull(spiralisTask.getHeader().getCreationTimestamp(), "Creation timestamp is null");
        assertNotNull(spiralisTask.getHeader().getDocumentType(), "Document Type id not found");
        assertNotNull(spiralisTask.getHeader().getIdentifier(), "Instance identifier not found in SBDH");
        assertNotNull(spiralisTask.getHeader().getProcess(), "Process missing from SBDH");
        assertNotNull(spiralisTask.getHeader().getReceiver(), "Receiver not obtained from SBDH");
        assertNotNull(spiralisTask.getHeader().getSender(), "Sender not obtained from SBDH");

        assertNotNull(spiralisTask.getTransmissionId(), "Seems we were not able to find the AS2 message-ID");
    }
    

    @Test
    public void testBaseFileName() throws Exception {

        // Obtains a url to our sample payload xml file
        final URL url = DummyFiles.samplePayloadUrl();
        final Path path = Paths.get(url.toURI());

        // Extracts the base name, i.e. the filename without the extension
        final String s = SpiralisTaskFactory.baseFileName(path);
        assertEquals(s, DummyFiles.SAMPLE_UUID);
    }

}