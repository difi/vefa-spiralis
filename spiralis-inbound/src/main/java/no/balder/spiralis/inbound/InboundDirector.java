package no.balder.spiralis.inbound;

import com.google.inject.Inject;
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
    private final LinkedBlockingQueue<SpiralisTask> createdTasksQueue;
    BlockingQueue<Path> scannedTasksQueue;


    @Inject
    InboundDirector(Path dirPath) {
        this.dirPath = dirPath;
        scannedTasksQueue = new LinkedBlockingDeque<>();
        createdTasksQueue = new LinkedBlockingQueue<>();
    }


    public void start() throws InterruptedException {

        final Future<Void> watchDirFuture = new ScanActivity(dirPath,scannedTasksQueue).invoke();

        new CreateTaskActivity(scannedTasksQueue, createdTasksQueue).invoke();


        // ------- Wait
        try {
            // Waits indefinitely for the watch dir task
            watchDirFuture.get();
        } catch (ExecutionException e) {
            throw new IllegalStateException("Interrupted while watching directories ", e);
        }
    }
}
