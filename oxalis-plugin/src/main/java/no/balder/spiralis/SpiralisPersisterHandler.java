package no.balder.spiralis;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.tool.gson.GsonHelper;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.commons.filesystem.FileUtils;
import no.difi.oxalis.commons.persist.PersisterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author steinar
 *         Date: 20.02.2017
 *         Time: 13.23
 */
public class SpiralisPersisterHandler implements ReceiptPersister {

    public static final Logger LOGGER = LoggerFactory.getLogger(SpiralisPersisterHandler.class);

    @Inject
    @Named("inbound")
    private Path inboundPath;

    @Inject
    @Named("default")
    private ReceiptPersister receiptPersister;

    @Override
    public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {

        // Emits the REM evidence etc.
        receiptPersister.persist(inboundMetadata, payloadPath);
        
        final Path receiptPath = PersisterUtils.createArtifactFolders(inboundPath, inboundMetadata.getHeader()).resolve(
                String.format("%s%s",
                        FileUtils.filterString(inboundMetadata.getTransmissionIdentifier().toString()),
                        WellKnownFileTypeSuffix.AS2_RECEIPT.getSuffix()));

        LOGGER.info("Persisting meta data: {}", inboundMetadata.getHeader());

        if (inboundMetadata.primaryReceipt() == null) {
            throw new IllegalStateException("inboundMetadata.primaryReceipt() returned null!");
        }


        Files.write(receiptPath, inboundMetadata.primaryReceipt().getValue());
        LOGGER.info("Transmission receipt of type {} written to {}",
                inboundMetadata.primaryReceipt().getType(),
                receiptPath);

        final Path jsonMetaDataPath = PersisterUtils.createArtifactFolders(inboundPath, inboundMetadata.getHeader()).resolve(
                String.format("%s%s", FileUtils.filterString(inboundMetadata.getTransmissionIdentifier().toString()),
                        WellKnownFileTypeSuffix.META_JSON.getSuffix())
        );

        String json = GsonHelper.toJson(inboundMetadata);
        Files.write(jsonMetaDataPath, json.getBytes("UTF-8"));
        LOGGER.info("Json metdata written to " + jsonMetaDataPath);

        LOGGER.info("Handling complete.");
    }
}
