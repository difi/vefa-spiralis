package no.balder.spiralis.inbound;

import com.google.inject.Inject;
import no.balder.spiralis.jdbc.SpiralisTaskPersister;
import no.balder.spiralis.payload.PayloadPathUtil;
import no.balder.spiralis.payload.PayloadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author steinar
 *         Date: 04.02.2017
 *         Time: 12.37
 */
class ProcessActivity {

    public static final Logger LOGGER = LoggerFactory.getLogger(ProcessActivity.class);

    public static final int N_THREADS = 8;
    private final BlockingQueue<SpiralisReceptionTask> createdTasksQueue;
    private final PayloadStore payloadStore;
    private final SpiralisTaskPersister spiralisTaskPersister;
    private final Path inboundRootPath;
    private final Path archivePath;
    int threadNumber = 0;
    private ExecutorService executorService;
    private AtomicLong processCount = new AtomicLong(0);

    @Inject
    public ProcessActivity(BlockingQueue<SpiralisReceptionTask> createdTasksQueue, PayloadStore payloadStore, SpiralisTaskPersister spiralisTaskPersister, Path inboundRootPath, Path archivePath) {
        this.createdTasksQueue = createdTasksQueue;
        this.payloadStore = payloadStore;
        this.spiralisTaskPersister = spiralisTaskPersister;
        this.inboundRootPath = inboundRootPath;
        this.archivePath = archivePath;

        if (PayloadPathUtil.overlaps(inboundRootPath, archivePath)) {
            throw new InvalidPathException("The paths overlap each other", inboundRootPath + " and " + archivePath + " can not be used in combination");
        }
    }

    public void startThreads() {
        startProcessActivity(createdTasksQueue);
    }

    private void startProcessActivity(BlockingQueue<SpiralisReceptionTask> createdTasksQueue) {

        executorService = Executors.newFixedThreadPool(N_THREADS);

        for (int i = 0; i < N_THREADS; i++) {
            executorService.submit(createSpiralisTaskProcessor(createdTasksQueue));
        }
    }

    private Callable<Void> createSpiralisTaskProcessor(BlockingQueue<SpiralisReceptionTask> createdTasksQueue) {
        return new Callable<Void>() {
            @Override
            public Void call() {

                int errorCount = 0;
                for (; ; ) {
                    // All errors needs to be catched in order to continue is something goes wrong
                    try {
                        SpiralisReceptionTask spiralisReceptionTask = null;
                        try {
                            spiralisReceptionTask = createdTasksQueue.take();
                        } catch (InterruptedException e) {
                            LOGGER.error("Error taking from queue: " + e.getMessage(), e);
                            continue;
                        }

                        final Path path = spiralisReceptionTask.getPayloadPath();

                        LOGGER.debug("Processing " + spiralisReceptionTask);

                        String blobName = BlobName.createInboundBlobName(spiralisReceptionTask, spiralisReceptionTask::getPayloadPath);

                        LOGGER.debug("Uploading " + path + " to " + blobName);
                        final URI payloadBlobUri = payloadStore.upload(path, blobName);
                        LOGGER.debug("Uploaded " + payloadBlobUri);

                        // Handles the transmission receipt, if present
                        URI smimeBlobUri = null;
                        if (spiralisReceptionTask.getSmimePath() != null) {
                            String smimeBlobName = BlobName.createInboundBlobName(spiralisReceptionTask, spiralisReceptionTask::getSmimePath);
                            LOGGER.debug("Uploading " + spiralisReceptionTask.getSmimePath() + " to " + smimeBlobName);
                            smimeBlobUri = payloadStore.upload(spiralisReceptionTask.getSmimePath(), smimeBlobName);
                            LOGGER.debug("Uploaded " + smimeBlobUri);
                        } else {
                            LOGGER.warn("No transmission receipt found for " + spiralisReceptionTask);
                        }

                        // Inserts the metadata into the database
                        final Long aLong = spiralisTaskPersister.saveInboundTask(spiralisReceptionTask, payloadBlobUri, Optional.ofNullable(smimeBlobUri));

                        // Mark payload as processed by moving the files into the archive
                        PayloadPathUtil.moveWithSubdirIntact(inboundRootPath, spiralisReceptionTask.getPayloadPath(), archivePath);

                        if (spiralisReceptionTask.getSmimePath() != null)
                            PayloadPathUtil.moveWithSubdirIntact(inboundRootPath, spiralisReceptionTask.getSmimePath(), archivePath);

                    } catch (Exception e) {
                        LOGGER.error("Error during processing " + e.getMessage(), e);
                        errorCount++;
                        if (errorCount > 100) {
                            LOGGER.error("More than 100 errors, bailing out");
                            break;
                        } else
                            continue;
                    }

                    processCount.incrementAndGet();
                }

                return null;
            }
        };
    }


    public Long getProcessedCounter() {
        return processCount.get();
    }
}
