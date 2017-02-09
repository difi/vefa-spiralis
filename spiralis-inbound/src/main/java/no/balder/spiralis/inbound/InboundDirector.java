package no.balder.spiralis.inbound;

import com.google.inject.Inject;
import no.balder.spiralis.jdbc.SpiralisTaskPersister;
import no.balder.spiralis.payload.PayloadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.*;

/**
 * Directs the following activities, which are furher broken into various tasks:
 * <ol>
 *     <li>{@link ScanActivity} - scans a directory tree for existing and new payload files together with any
 *     associated metadata files and transmission evidence files</li>
 *     <li>{@link CreateTaskActivity} - parses the files and creates {@link SpiralisReceptionTask} objects, which are passed on to</li>
 *     <li>{@link ProcessActivity} - processes the {@link SpiralisReceptionTask} instances.</li>
 * </ol>
 *
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 16.18
 */
public class InboundDirector {

    public static final Logger LOGGER = LoggerFactory.getLogger(InboundDirector.class);

    private final Path inboundDirPath;
    private final Path archiveDirPath;
    private final String fileMatchGlob;
    private final PayloadStore payloadStore;
    private final SpiralisTaskPersister spiralisTaskPersister;

    // Holds the paths of the scanned input files.
    private final BlockingQueue<Path> scannedTasksQueue;

    // Holds the SpiralisReceptionTask instances, which have been created from the scanned paths
    private final LinkedBlockingQueue<SpiralisReceptionTask> createdTasksQueue;

    // Holds references to the activity instances, which in turn may contain several tasks being run in
    // threads.
    private ScanActivity scanActivity;
    private CreateTaskActivity createTaskActivity;
    private ProcessActivity processActivity;


    /**
     * @param inboundDirPath root path of the directory tree to scan and watch.
     * @param archiveDirPath root path in which the processed files should be archived.
     * @param fileMatchGlob the "glob" used to match files during scanning in the {@link ScanActivity}
     * @param payloadStore the {@link PayloadStore} instance to be used in the final {@link ProcessActivity}
     */
    @Inject
    InboundDirector(Path inboundDirPath, Path archiveDirPath, String fileMatchGlob, PayloadStore payloadStore, SpiralisTaskPersister spiralisTaskPersister) {
        this.inboundDirPath = inboundDirPath;
        this.archiveDirPath = archiveDirPath;
        this.fileMatchGlob = fileMatchGlob;
        this.payloadStore = payloadStore;
        this.spiralisTaskPersister = spiralisTaskPersister;
        scannedTasksQueue = new LinkedBlockingDeque<>();
        createdTasksQueue = new LinkedBlockingQueue<>();
    }


    public void startThreads() throws InterruptedException {

        // Starts scanning the root directory path for existing and new files to be processed
        scanActivity = new ScanActivity(inboundDirPath, fileMatchGlob, scannedTasksQueue);
        scanActivity.startThreads();

        // For each scanned path, creates work order requests in as SpiralisReceptionTask
        createTaskActivity = new CreateTaskActivity(scannedTasksQueue, createdTasksQueue);
        createTaskActivity.invoke();

        // Finally, we fire up the threads that will process the SpiralisReceptionTask instances.
        processActivity = new ProcessActivity(createdTasksQueue, payloadStore, spiralisTaskPersister, inboundDirPath, archiveDirPath);
        processActivity.startThreads();
    }

    public Statistics getProcessingStatistics() {
        final Long scanned = scanActivity.getProcessedCounter();
        final Long created = createTaskActivity.getProcessedCounter();
        final Long processed = processActivity.getProcessedCounter();

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
