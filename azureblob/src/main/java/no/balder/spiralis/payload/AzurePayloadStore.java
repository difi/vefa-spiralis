package no.balder.spiralis.payload;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 16.05
 */
public class AzurePayloadStore implements PayloadStore {

    public static final Logger LOGGER = LoggerFactory.getLogger(AzurePayloadStore.class);

    private final CloudBlobClient blobClient;

    public AzurePayloadStore(String connectionString) {
        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = null;
        try {
            storageAccount = CloudStorageAccount.parse(connectionString);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to connecto to Azure " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid connection key: " + connectionString);
        }

        // Create the payload client.
        blobClient = storageAccount.createCloudBlobClient();
    }

    @Override
    public URI upload(Path payload, String sender, String receiver, OffsetDateTime offsetDateTime) {

        final CloudBlobContainer cloudBlobContainer = containerReferenceFor(payload);
        try {
            final CloudBlockBlob blockBlobReference = cloudBlobContainer.getBlockBlobReference(payload.toString());
            blockBlobReference.uploadFromFile(payload.toString());
            return blockBlobReference.getUri();
        } catch (URISyntaxException |StorageException | IOException e) {
            throw new IllegalStateException("Unable to upload " + payload + "; " + e.getMessage(), e);
        }
    }

    private CloudBlobContainer containerReferenceFor(Path payload) {

        final String containerName = ContainerUtil.containerNameFor(payload);
        try {
            // TODO: consider caching this
            final CloudBlobContainer containerReference = blobClient.getContainerReference(containerName);
            containerReference.createIfNotExists();
            return containerReference;

        } catch (URISyntaxException | StorageException e) {
            throw new IllegalStateException("Unable to obtain container reference for " + containerName);
        }
    }

}
