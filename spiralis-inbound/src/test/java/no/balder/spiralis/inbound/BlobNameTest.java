package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.PayloadPathUtil;
import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.testutil.DummyFiles;
import no.difi.vefa.peppol.common.model.*;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Date;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 11.03
 */
public class BlobNameTest {


    @Test   (enabled = false)
    public void testCreate() throws Exception {

        final URL url = DummyFiles.samplePayloadUrl();
        final InstanceType instanceType = InstanceType.of("UBL", "ttt", "v1");
        final InstanceIdentifier instanceIdentifier = InstanceIdentifier.generateUUID();

        Header header =  Header.of(ParticipantIdentifier.of("9908:976098897"), ParticipantIdentifier.of("9908:123321678"), ProcessIdentifier.of("rubbish"), DocumentTypeIdentifier.of("dummy"), instanceIdentifier, instanceType, new Date());
        final URI uri = url.toURI();
        System.out.println("Creating path from : " + uri.toString());
        final Path payloadPath = Paths.get(uri);
        final SpiralisReceptionTask spiralisReceptionTask = new SpiralisReceptionTask(payloadPath, header);
        spiralisReceptionTask.setReceived(OffsetDateTime.now());

        // Creates the path of the S/MIME file
        final String smimeFileName = payloadPath.getFileName().toString().replace(WellKnownFileTypeSuffix.PAYLOAD.getSuffix(), WellKnownFileTypeSuffix.AS2_RECEIPT.getSuffix());
        final Path smimePath = payloadPath.getParent().resolve(smimeFileName);
        spiralisReceptionTask.setSmimePath(smimePath);
        
        final String newPayloadBlobName = BlobName.createInboundBlobName(spiralisReceptionTask, spiralisReceptionTask::getPayloadPath);
        assertTrue(newPayloadBlobName.contains(spiralisReceptionTask.getReceptionId().toString()),"Seems the blob name does not contain the UUID of the reception");
        assertFalse(newPayloadBlobName.contains(PayloadPathUtil.fileNameBodyPart(spiralisReceptionTask.getPayloadPath())), "Blob name should not contain the name of the original file");
        System.out.println(newPayloadBlobName);

        final String newSmimeBlobName = BlobName.createInboundBlobName(spiralisReceptionTask, spiralisReceptionTask::getSmimePath);
        assertTrue(newSmimeBlobName.contains(spiralisReceptionTask.getReceptionId().toString()));
        System.out.println(newSmimeBlobName);

    }

}