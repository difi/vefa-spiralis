package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.testutil.DummyFiles;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 19.06
 */
public class PayloadClassifierTest {


    private Path inboundDummyFiles;

    @BeforeMethod
    public void setUp() throws Exception {
        inboundDummyFiles = DummyFiles.createInboundDummyFiles();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        DummyFiles.removeAll(inboundDummyFiles);
    }

    @Test
    public void testClassify() throws Exception {

        Files.createTempFile(inboundDummyFiles, "test", ".rubbish");
        final DirectoryStream<Path> stream = Files.newDirectoryStream(inboundDummyFiles);

        int unknown = 0;
        int receipt = 0;
        int payload = 0;
        int rem = 0;

        for (Path path : stream) {
            final Optional<WellKnownFileTypeSuffix> classification = PayloadClassifier.classify(path);
            if (!classification.isPresent()) {
                unknown++;
            } else {
                final WellKnownFileTypeSuffix suffix = classification.get();
                switch (suffix) {
                    case AS2_RECEIPT:
                        receipt++;
                        break;
                    case PAYLOAD:
                        payload++;
                        break;
                    case REM_EVIDENCE:
                        rem++;
                        break;
                    default:
                        throw new IllegalStateException("There is a bug in your test!");
                }
            }
        }

        assertEquals(unknown, 1, "Did not manage to classify the unknonw");
        assertEquals(receipt, 1, "Receipt not classified");
        assertEquals(payload, 1, "Payload not classified");
        assertEquals(rem, 1, "REM not classified");

    }

}