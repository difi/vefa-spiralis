package no.balder.spiralis.inbound;

import com.google.inject.Inject;
import no.balder.spiralis.payload.PayloadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.*;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 16.18
 */
public class InboundDirector {

    public static final Logger LOGGER = LoggerFactory.getLogger(InboundDirector.class);

    private final Path dirPath;
    private final String fileMatchGlob;
    private final PayloadStore payloadStore;
    private final BlockingQueue<Path> scannedTasksQueue;
    private final LinkedBlockingQueue<SpiralisTask> createdTasksQueue;
    private ScanActivity scanActivity;
    private CreateTaskActivity createTaskActivity;
    private ProcessActivity processActivity;


    @Inject
    InboundDirector(Path dirPath, String fileMatchGlob, PayloadStore payloadStore) {
        this.dirPath = dirPath;
        this.fileMatchGlob = fileMatchGlob;
        this.payloadStore = payloadStore;
        scannedTasksQueue = new LinkedBlockingDeque<>();
        createdTasksQueue = new LinkedBlockingQueue<>();
    }


    public void start() throws InterruptedException {

        // Starts scanning the root directory path for existing and new files to be processed
        scanActivity = new ScanActivity(dirPath, fileMatchGlob, scannedTasksQueue);
        scanActivity.startThreads();

        // For each scanned path, creates work order requests in as SpiralisTask
        createTaskActivity = new CreateTaskActivity(scannedTasksQueue, createdTasksQueue);
        createTaskActivity.invoke();

        // Finally, we fire up the threads that will process the SpiralisTask instances.
        processActivity = new ProcessActivity(createdTasksQueue, payloadStore);
        processActivity.invoke();
    }

    public Statistics getProcessingStatistics() {
        final Long scanned = scanActivity.getProcessedCounter();
        final Long created = createTaskActivity.getProcessCount();
        final Long processed = processActivity.getProcessCount();

        return  new Statistics(scanned, created, processed);
    }

    class Statistics {
        private final Long scanned;
        private final Long created;
        private final Long processed;

        public Statistics(Long scanned, Long created, Long processed) {
            this.scanned = scanned;
            this.created = created;
            this.processed = processed;
        }

        public Long getScanned() {
            return scanned;
        }

        public Long getCreated() {
            return created;
        }

        public Long getProcessed() {
            return processed;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Statistics{");
            sb.append("scanned=").append(scanned);
            sb.append(", created=").append(created);
            sb.append(", processed=").append(processed);
            sb.append('}');
            return sb.toString();
        }
    }
}
