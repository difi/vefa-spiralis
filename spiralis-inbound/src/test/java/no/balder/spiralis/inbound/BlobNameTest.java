package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.ReceptionPathUtil;
import no.balder.spiralis.testutil.DummyFiles;
import no.balder.spiralis.transport.ReceptionId;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 11.03
 */
public class BlobNameTest {


    @Test   (enabled = true)
    public void testCreate() throws Exception {

        final Path rootDir = DummyFiles.createInboundDummyFilesInRootWithOptionalSubdirs();
        final List<Path> paths = DummyFiles.locatePayloadFilesIn(rootDir);

        final Path payloadPath = paths.get(0);


        final ReceptionId receptionId = new ReceptionId();
        final Date timeStamp = new Date();
        final String orgNo = "976098897";
        final ParticipantIdentifier sender = ParticipantIdentifier.of("9908:" + orgNo);
        final String newPayloadBlobName = BlobName.createInboundBlobName(receptionId, timeStamp, sender, payloadPath);

        assertTrue(newPayloadBlobName.contains(receptionId.toString()),"Seems the blob name does not contain the UUID of the reception");
        assertFalse(newPayloadBlobName.contains(ReceptionPathUtil.fileNameBodyPart(payloadPath)), "Blob name should not contain the name of the original file");
        System.out.println(newPayloadBlobName);

    }

}