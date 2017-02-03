package no.balder.spiralis;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.SharedAccessPolicy;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class ConnectionTest {


    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;"
                    + "AccountName=hmaptestdata01;" // account name
                    + "AccountKey=eOAASM/8sBRm9/7xvmp+V5qm0Mg6UsiNQ2DUtjrZY6upqfeU5xvVBYSpCAic1J+QqkCfREjmwsXIqQ9SaL+78g==";   // access key
    public static final String SAMPLE_INVOICE_XML = "sample-invoice.xml";
    private URL sampleInvoiceURLOnLocalDisk;


    @BeforeMethod
    public void setUp() throws Exception {
        sampleInvoiceURLOnLocalDisk = ConnectionTest.class.getClassLoader().getResource(SAMPLE_INVOICE_XML);
        assertNotNull(sampleInvoiceURLOnLocalDisk, SAMPLE_INVOICE_XML + " not found in class path");
    }

    @Test
    public void testConnectToAzure() throws Exception {

        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        CloudBlobContainer container = blobClient.getContainerReference("mycontainer");

        // Create the container if it does not exist.
        container.createIfNotExists();
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
                CloudBlockBlob blob = container.getBlockBlobReference(SAMPLE_INVOICE_XML);
            File sourceFile = new File(sampleInvoiceURLOnLocalDisk.toURI());
            assertTrue(sourceFile.canRead(), "No read access to " + sampleInvoiceURLOnLocalDisk);

            blob.upload(new FileInputStream(sourceFile), sourceFile.length());

            // Download the image file.
            String tmpDirName = System.getProperty("java.io.tmpdir");
            File destinationFile = new File(tmpDirName, "image1Download.tmp");
            blob.downloadToFile(destinationFile.getAbsolutePath());

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
    public void testTmpFile() throws Exception {
        System.out.println(System.getProperty("java.io.tmpdir"));
    }

    @Test
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
        CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
        CloudBlobClient serviceClient = account.createCloudBlobClient();

        // Container name must be lower case.
        CloudBlobContainer container = serviceClient.getContainerReference("invoice-out");


        for (ListBlobItem blobItem : container.listBlobs()) {
            System.out.println(blobItem.getUri());

            CloudBlockBlob blockBlobReference = container.getBlockBlobReference(SAMPLE_INVOICE_XML);

            System.out.println(blockBlobReference.getName());

            SharedAccessBlobPolicy sharedAccessPolicy = createSharedAccessPolicy(EnumSet.of(SharedAccessBlobPermissions.READ), 300);
            String sharedUri = blockBlobReference.generateSharedAccessSignature(sharedAccessPolicy, null);

            
            String s = blockBlobReference.getUri().toString() + "?" + sharedUri;
            System.out.println(s);

        }


    }

    @Test
    public void testModulus() throws Exception {

        String format = "yyyyMMddHHmmssSSS";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);

        for (int i=0; i < 100; i++){

            LocalDateTime ldt = LocalDateTime.now();
            String format1 = ldt.format(dateTimeFormatter);
            long l = Long.parseLong(format1);

            long m =  (l % 300);
            System.out.printf("%d -> %d\n", l, m);
            Thread.sleep(1);
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
}

