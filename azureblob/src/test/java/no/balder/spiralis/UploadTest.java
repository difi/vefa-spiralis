package no.balder.spiralis;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.concurrent.*;

import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 02.02.2017
 *         Time: 11.59
 */
public class UploadTest {

    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;"
                    + "AccountName=hmaptestdata01;" // account name
                    + "AccountKey=eOAASM/8sBRm9/7xvmp+V5qm0Mg6UsiNQ2DUtjrZY6upqfeU5xvVBYSpCAic1J+QqkCfREjmwsXIqQ9SaL+78g==";   // access key
    int count;

    @Test(enabled = false)
    public void testIterate() throws Exception {

        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("/var/peppol/samples"));

        stream.forEach(p -> {
            System.out.println(p.getFileName().toString());
            count++;
        });
        System.out.println("Antall " + count);
    }

    /**
     * Uploads all files in /var/peppol/samples sequentially using a single thread.
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void testUploadManyFiles() throws Exception {

        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("/var/peppol/samples"));


        long start = System.nanoTime();
        try {
            CloudBlobContainer container = getCloudBlobContainer();

            stream.forEach(p -> {

                long startUpload = System.nanoTime();
                // Uploads an invoice
                String blobName = p.getFileName().toString();
                try {
                    CloudBlockBlob blob = container.getBlockBlobReference(blobName);
                    File sourceFile = p.toFile();
                    assertTrue(sourceFile.canRead(), "No read access to " + p.toString());

                    blob.upload(new FileInputStream(sourceFile), sourceFile.length());

                } catch (StorageException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                long elapsed = System.nanoTime() - startUpload;

                elapsed = TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS);

                System.out.println(blobName + " took " + elapsed + "ms");

            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        long end = System.nanoTime();
        long elapsed = end - start;

        System.out.println("Elapsed " + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS)+"ms");
    }


    /**
     * Uploads all files in /var/peppol/samples in parallell.
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void testUploadInParallell() throws Exception {

        long start = System.nanoTime();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("/var/peppol/samples"));

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ExecutorCompletionService<Long> completionService = new ExecutorCompletionService<>(executorService);

        CloudBlobContainer container = getCloudBlobContainer();

        count = 0;
        directoryStream.forEach(path -> {

            Callable<Long> callable = new Callable<Long>() {

                final long id = count;

                @Override
                public Long call() throws Exception {
                    long startUpload = System.nanoTime();
                    // Uploads an invoice
                    String blobName = path.getFileName().toString();
                    try {
                        CloudBlockBlob blob = container.getBlockBlobReference(blobName);
                        File sourceFile = path.toFile();
                        assertTrue(sourceFile.canRead(), "No read access to " + path.toString());

                        blob.upload(new FileInputStream(sourceFile), sourceFile.length());

                    } catch (StorageException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    long elapsed = System.nanoTime() - startUpload;

                    elapsed = TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS);

                    System.out.println(id + " " + blobName + " took " + elapsed + "ms");

                    return elapsed;
                }
            };

            Future<Long> longFuture = completionService.submit(callable);
            count++;

        });


        long sum=0;
        for (int i =0; i < count; i++) {
            try {
                Future<Long> taken = completionService.take();
                Long aLong = taken.get();
                sum += aLong;
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Ooops failed fetching result " + i + " of " + count+ "; " + e.getMessage() );
            }
        }

        long elapsedMs = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        System.out.println("Uploaded " + count + " invoices in " + elapsedMs + "ms, average was " + (elapsedMs/count) + "ms per invoice");

    }

    private CloudBlobContainer getCloudBlobContainer() throws URISyntaxException, InvalidKeyException, StorageException {
        CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
        CloudBlobClient serviceClient = account.createCloudBlobClient();

        // Container name must be lower case.
        CloudBlobContainer container = serviceClient.getContainerReference("invoice-out");
        container.createIfNotExists();
        return container;
    }
}
