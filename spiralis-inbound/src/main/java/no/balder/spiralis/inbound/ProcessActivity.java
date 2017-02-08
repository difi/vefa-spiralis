package no.balder.spiralis.inbound;

import com.google.inject.Inject;
import no.balder.spiralis.jdbc.SpiralisTaskPersister;
import no.balder.spiralis.payload.PayloadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author steinar
 *         Date: 04.02.2017
 *         Time: 12.37
 */
class ProcessActivity {

    public static final Logger LOGGER = LoggerFactory.getLogger(ProcessActivity.class);

    public static final int N_THREADS = 8;
    private final LinkedBlockingQueue<SpiralisTask> createdTasksQueue;
    private final PayloadStore payloadStore;
    private final SpiralisTaskPersister spiralisTaskPersister;
    int threadNumber = 0;
    private ExecutorService executorService;
    private AtomicLong processCount = new AtomicLong(0);

    @Inject
    public ProcessActivity(LinkedBlockingQueue<SpiralisTask> createdTasksQueue, PayloadStore payloadStore, SpiralisTaskPersister spiralisTaskPersister) {
        this.createdTasksQueue = createdTasksQueue;
        this.payloadStore = payloadStore;
        this.spiralisTaskPersister = spiralisTaskPersister;
    }

    public void invoke() {
        startProcessActivity(createdTasksQueue);
    }

    private void startProcessActivity(LinkedBlockingQueue<SpiralisTask> createdTasksQueue) {

        executorService = Executors.newFixedThreadPool(N_THREADS);

        for (int i = 0; i < N_THREADS; i++) {
            executorService.submit(createSpiralisTaskProcessor(createdTasksQueue));
        }
    }

    private Callable<Void> createSpiralisTaskProcessor(LinkedBlockingQueue<SpiralisTask> createdTasksQueue) {
        return new Callable<Void>() {
            @Override
            public Void call() {

                int errorCount = 0;
                for (; ; ) {
                    // All errors needs to be catched in order to continue is something goes wrong
                    try {
                        SpiralisTask spiralisTask = null;
                        try {
                            spiralisTask = createdTasksQueue.take();
                            processCount.incrementAndGet();
                        } catch (InterruptedException e) {
                            LOGGER.error("Error taking from queue: " + e.getMessage(), e);
                            continue;
                        }
                        final Path path = spiralisTask.getPayloadPath();

                        LOGGER.debug("Processing " + spiralisTask);

                        String blobName = BlobName.createInboundBlobName(spiralisTask, spiralisTask::getPayloadPath);
                        LOGGER.debug("Uploading " + path + " to " + blobName);
                        final URI payloadBlobUri = payloadStore.upload(path, blobName);
                        LOGGER.debug("Uploaded " + payloadBlobUri);

                        // Handles the transmission receipt, if present
                        URI smimeBlobUri = null;
                        if (spiralisTask.getSmimePath() != null) {
                            String smimeBlobName = BlobName.createInboundBlobName(spiralisTask, spiralisTask::getSmimePath);
                            LOGGER.debug("Uploading " + spiralisTask.getSmimePath() + " to " + smimeBlobName);
                            smimeBlobUri = payloadStore.upload(spiralisTask.getSmimePath(), smimeBlobName);
                            LOGGER.debug("Uploaded " + smimeBlobUri);
                        } else {
                            LOGGER.warn("No transmission receipt found for " + spiralisTask);
                        }

                        // Inserts the metadata into the database
                        final Long aLong = spiralisTaskPersister.saveInboundTask(spiralisTask, payloadBlobUri, Optional.ofNullable(smimeBlobUri));

                        // Mark payload as processed
                        

                    } catch (Exception e) {
                        LOGGER.error("Error during processing " + e.getMessage(), e);
                        errorCount++;
                        if (errorCount > 100) {
                            LOGGER.error("More than 100 errors, bailing out");
                            break;
                        } else
                            continue;
                    }
                }

                return null;
            }
        };
    }


    public Long getProcessedCounter() {
        return processCount.get();
    }
}
