package no.balder.spiralis;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.typesafe.config.Config;
import no.balder.spiralis.config.AzureBlobTestModuleFactory;
import no.balder.spiralis.config.SpiralisConfigProperty;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.StreamSupport;

import static no.balder.spiralis.TestResources.SAMPLE_INVOICE_XML;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
@Guice(moduleFactory = AzureBlobTestModuleFactory.class)
public class AzureMethodsTest {

    @Inject
    @Named(SpiralisConfigProperty.SPIRALIS_AZURE_CONNECT)
    public static String storageConnectionString;
    @Inject
    Config config;
    private URL sampleInvoiceURLOnLocalDisk;


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

    @BeforeMethod
    public void setUp() throws Exception {
        sampleInvoiceURLOnLocalDisk = AzureMethodsTest.class.getClassLoader().getResource(SAMPLE_INVOICE_XML);
        assertNotNull(sampleInvoiceURLOnLocalDisk, SAMPLE_INVOICE_XML + " not found in class path");

        storageConnectionString = config.getString(SpiralisConfigProperty.SPIRALIS_AZURE_CONNECT);
    }

    @Test
    public void testConnectToAzure() throws Exception {

        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

        // Create the payload client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        CloudBlobContainer container = blobClient.getContainerReference("mycontainer");

        // Create the container if it does not exist.
        container.createIfNotExists();
    }


    @Test(enabled = false)
    public void deleteContainers() throws Exception {

        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

        // Create the payload client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        CloudBlobContainer container = blobClient.getContainerReference("mycontainer");
        container.deleteIfExists();
        container = blobClient.getContainerReference("outbox");
        container.deleteIfExists();
    }

    @Test(enabled = false)
    public void deletePeppolApContainers() throws Exception {
        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

        // Create the payload client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        final Iterable<CloudBlobContainer> cloudBlobContainers = blobClient.listContainers("peppol-ap");
        cloudBlobContainers.forEach(cloudBlobContainer -> {
            final String name = cloudBlobContainer.getName();
            try {
                cloudBlobContainer.deleteIfExists();
            } catch (StorageException e) {


            }
            System.out.println(name + " deleted");
        });


    }

    @Test(enabled = false)
    public void testListContainers() throws Exception {
        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

        // Create the payload client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();


        final Iterable<CloudBlobContainer> cloudBlobContainers = blobClient.listContainers();
        for (CloudBlobContainer cloudBlobContainer : cloudBlobContainers) {
            System.out.println(cloudBlobContainer.getName());
        }
    }


    @Test(enabled = false)
    public void listBlobCountInPeppolContainers() throws Exception {

        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

        // Create the payload client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();


        final Iterable<CloudBlobContainer> cloudBlobContainers = blobClient.listContainers();
        for (CloudBlobContainer cloudBlobContainer : cloudBlobContainers) {
            if (cloudBlobContainer.getName().startsWith("peppol")) {

                final Iterable<ListBlobItem> listBlobItems = cloudBlobContainer.listBlobs("",true);
                final long count = StreamSupport.stream(listBlobItems.spliterator(), false).count();
                System.out.println(cloudBlobContainer.getName() + " : " + count);
            } else
                System.out.println("Skipping " + cloudBlobContainer.getName());
        }
    }

    @Test(enabled = false)
    public void listPeppolApBlobsInAllContainers() throws URISyntaxException, InvalidKeyException {
        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

        // Create the payload client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        final Iterable<CloudBlobContainer> cloudBlobContainers = blobClient.listContainers();
        for (CloudBlobContainer cloudBlobContainer : cloudBlobContainers) {
            if (cloudBlobContainer.getName().startsWith("peppol")) {

                final Iterable<ListBlobItem> listBlobItems = cloudBlobContainer.listBlobs("",true);
                for (ListBlobItem listBlobItem : listBlobItems) {
                    System.out.println(listBlobItem.getUri());
                }
            }
        }
    }

    @Test
    public void testUpAndDownLoad() throws Exception {

        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            // Container name must be lower case.
            CloudBlobContainer container = serviceClient.getContainerReference("invoice-out");
            container.createIfNotExists();

            // Upload an invoice
            final String blobName = SAMPLE_INVOICE_XML;

            CloudBlockBlob blob = container.getBlockBlobReference(blobName);
            File sourceFile = new File(sampleInvoiceURLOnLocalDisk.toURI());
            assertTrue(sourceFile.canRead(), "No read access to " + sampleInvoiceURLOnLocalDisk);

            blob.upload(new FileInputStream(sourceFile), sourceFile.length());

            // Downloads the  SAMPLE file.
            String tmpDirName = System.getProperty("java.io.tmpdir");
            File destinationFile = new File(tmpDirName, SAMPLE_INVOICE_XML);
            blob.downloadToFile(destinationFile.getAbsolutePath());
            System.out.println(blob.getUri());

        } catch (FileNotFoundException fileNotFoundException) {
            System.out.print("FileNotFoundException encountered: ");
            System.out.println(fileNotFoundException.getMessage());
            System.exit(-1);
        } catch (StorageException storageException) {
            System.out.print("StorageException encountered: ");
            System.out.println(storageException.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.out.print("Exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    @Test
    public void uploadWithDirectoriesAndList() throws Exception {
        CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
        CloudBlobClient serviceClient = account.createCloudBlobClient();

        // Container name must be lower case.
        CloudBlobContainer container = serviceClient.getContainerReference("invoice-out");
        container.createIfNotExists();

        // Uploads an invoice
        final String blobName = "test/" + SAMPLE_INVOICE_XML;

        CloudBlockBlob blob = container.getBlockBlobReference(blobName);
        File sourceFile = new File(sampleInvoiceURLOnLocalDisk.toURI());
        assertTrue(sourceFile.canRead(), "No read access to " + sampleInvoiceURLOnLocalDisk);

        blob.upload(new FileInputStream(sourceFile), sourceFile.length());


        // Attempts to list everything
        final Iterable<ListBlobItem> blobItems = container.listBlobs("test");
        for (ListBlobItem blobItem : blobItems) {
            System.out.println(blobItem.getClass().getName());
            System.out.println("uri:" + blobItem.getUri());
        }

        System.out.println("---------------------------");

        // Directory listing
        final CloudBlobDirectory testDir = container.getDirectoryReference("test");

        final Iterable<ListBlobItem> listBlobItems = testDir.listBlobs();
        for (ListBlobItem listBlobItem : listBlobItems) {
            System.out.println(listBlobItem.getUri());
            if (listBlobItem instanceof CloudBlobDirectory) {
                CloudBlobDirectory dir = (CloudBlobDirectory) listBlobItem;
                final Iterable<ListBlobItem> blobItems1 = dir.listBlobs();
                for (ListBlobItem blobItem : blobItems1) {
                    System.out.println(blobItem.getUri());
                }
            }
        }
    }


    @Test(enabled = false)
    public void testListContents() throws Exception {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            // Container name must be lower case.
            CloudBlobContainer container = serviceClient.getContainerReference("invoice-out");
            container.createIfNotExists();

            for (ListBlobItem blobItem : container.listBlobs()) {
                System.out.println(blobItem.getUri());


            }
        } catch (StorageException e) {
            System.err.println("Ooops " + e.getMessage());
        }
    }

    @Test
    public void testBlobSasUri() throws Exception {
        // Connects to Azure
        CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);

        // Establishes a client to make life easy
        CloudBlobClient serviceClient = account.createCloudBlobClient();

        // Grabs a reference to our container.
        CloudBlobContainer container = serviceClient.getContainerReference("invoice-out");

        // ... and to our blob
        CloudBlockBlob blockBlobReference = container.getBlockBlobReference(SAMPLE_INVOICE_XML);

        // Generates the shared access policy which will be used in the next step.
        SharedAccessBlobPolicy sharedAccessPolicy = createSharedAccessPolicy(EnumSet.of(SharedAccessBlobPermissions.READ), 300);

        // Generates the SAS token.
        String sharedUri = blockBlobReference.generateSharedAccessSignature(sharedAccessPolicy, null);
        // Creates a complete URI, which can be used for download using for example your browser.
        String firstBlobUri = blockBlobReference.getUri().toString() + "?" + sharedUri;

        System.out.println(firstBlobUri);

        // Attempts to create a complete URI with SAS token, having only the URI of the blob
        final URI uriOfBlob = blockBlobReference.getUri();
        // Creates a new blob reference, using the credentials from our account

        final CloudBlockBlob blob2 = new CloudBlockBlob(uriOfBlob, account.getCredentials());
        // Generates the SAS token.
        final String sas2 = blob2.generateSharedAccessSignature(sharedAccessPolicy, null);
        // Creates the complete second URI with SAS token
        final String secondBlobUri = blob2.getUri().toString() + "?" + sas2;

        // They should be identical
        assertEquals(firstBlobUri, secondBlobUri);

        System.out.println("Second uri:" + secondBlobUri);
    }


    @Test
    public void testModulus() throws Exception {

        String format = "yyyyMMddHHmmssSSS";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);

        for (int i = 0; i < 100; i++) {

            LocalDateTime ldt = LocalDateTime.now();
            String format1 = ldt.format(dateTimeFormatter);
            long l = Long.parseLong(format1);

            long m = (l % 300);
            System.out.printf("%d -> %d\n", l, m);
            Thread.sleep(1);
        }

    }
}

