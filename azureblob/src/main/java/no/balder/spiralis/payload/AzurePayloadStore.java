package no.balder.spiralis.payload;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import no.balder.spiralis.config.SpiralisConfigProperty;
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

    @Inject
    public AzurePayloadStore(@Named(SpiralisConfigProperty.SPIRALIS_AZURE_CONNECT) String connectionString) {
        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = null;
        try {
            storageAccount = CloudStorageAccount.parse(connectionString);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to connecto to Azure " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid connection key: " + connectionString);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to connect with '" + connectionString + "', reason:" + e.getMessage(), e);
        }

        // Create the payload client.
        blobClient = storageAccount.createCloudBlobClient();
    }

    @Override
    public URI upload(Path payload, String blobName) {

        final CloudBlobContainer cloudBlobContainer = containerReferenceFor(payload);
        try {
            final CloudBlockBlob blockBlobReference = cloudBlobContainer.getBlockBlobReference(blobName);
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
