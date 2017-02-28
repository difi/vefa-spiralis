package no.balder.spiralis.payload;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
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
import java.util.*;

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
            throw new IllegalStateException("Unable to connect to to Azure " + e.getMessage(), e);
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

    @Override
    public URI createUriWithAccessToken(URI uriOfBlob) {
        if (uriOfBlob == null) {
            throw new IllegalArgumentException("Required argument 'uriOfBlob' is null");
        }
        String blobUri =null ;
        try {
            final CloudBlockBlob blob = new CloudBlockBlob(uriOfBlob, blobClient.getCredentials());
            // Generates the shared access policy which will be used in the next step.
            SharedAccessBlobPolicy sharedAccessPolicy = createSharedAccessPolicy(EnumSet.of(SharedAccessBlobPermissions.READ), 300);

            // Generates the SAS token.
            final String sas2 = blob.generateSharedAccessSignature(sharedAccessPolicy, null);

            // Creates the complete second URI with SAS token
            blobUri = blob.getUri().toString() + "?" + sas2;
            return new URI(blobUri);
        } catch (StorageException e) {
            throw new IllegalArgumentException("Unable to create block blob reference for " + uriOfBlob, e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Unable to create SAS key for " + uriOfBlob, e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to create new URI from " + blobUri + ";" + e.getMessage(), e);
        }
    }

    private final static SharedAccessBlobPolicy createSharedAccessPolicy(EnumSet<SharedAccessBlobPermissions> sap,
                                                                         int expireTimeInSeconds) {

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.add(Calendar.SECOND, expireTimeInSeconds);
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        policy.setPermissions(sap);
        policy.setSharedAccessExpiryTime(cal.getTime());
        return policy;

    }

    private CloudBlobContainer containerReferenceFor(Path payload) {

        final String containerName = ContainerUtil.containerNameFor(payload);
        try {
            final CloudBlobContainer containerReference = blobClient.getContainerReference(containerName);
            containerReference.createIfNotExists(); // TODO: consider caching this
            return containerReference;

        } catch (URISyntaxException | StorageException e) {
            throw new IllegalStateException("Unable to obtain container reference for " + containerName);
        }
    }

}
