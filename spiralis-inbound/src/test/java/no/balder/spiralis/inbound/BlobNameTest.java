package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.testutil.DummyFiles;
import no.difi.vefa.peppol.common.model.*;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 11.03
 */
public class BlobNameTest {


    @Test
    public void testCreate() throws Exception {

        final URL url = DummyFiles.samplePayloadUrl();
        final InstanceType instanceType = InstanceType.of("UBL", "ttt", "v1");
        final InstanceIdentifier instanceIdentifier = InstanceIdentifier.generateUUID();

        Header header =  Header.of(ParticipantIdentifier.of("9908:976098897"), ParticipantIdentifier.of("9908:123321678"), ProcessIdentifier.of("rubbish"), DocumentTypeIdentifier.of("dummy"), instanceIdentifier, instanceType, new Date());
        final Path payloadPath = Paths.get(url.toURI());
        final SpiralisTask spiralisTask = new SpiralisTask(payloadPath, header);
        spiralisTask.setReceived(OffsetDateTime.now());

        // Creates the path of the S/MIME file
        final String smimeFileName = payloadPath.getFileName().toString().replace(WellKnownFileTypeSuffix.PAYLOAD.getSuffix(), WellKnownFileTypeSuffix.AS2_RECEIPT.getSuffix());
        final Path smimePath = payloadPath.getParent().resolve(smimeFileName);
        spiralisTask.setSmimePath(smimePath);
        final String s = BlobName.createInboundBlobName(spiralisTask, spiralisTask::getPayloadPath);
        final String smime = BlobName.createInboundBlobName(spiralisTask, spiralisTask::getSmimePath);
        System.out.println(s);
        System.out.println(smime);

    }

}