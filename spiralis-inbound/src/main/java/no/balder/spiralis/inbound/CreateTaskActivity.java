package no.balder.spiralis.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.*;

/**
 * @author steinar
 *         Date: 04.02.2017
 *         Time: 12.18
 */
class CreateTaskActivity {

    public static final Logger LOGGER = LoggerFactory.getLogger(CreateTaskActivity.class);

    private final BlockingQueue<Path> scannedTasksQueue;
    private final BlockingQueue<SpiralisTask> createdTasksQueue;

    public CreateTaskActivity(BlockingQueue<Path> scannedTasksQueue, BlockingQueue<SpiralisTask> createdTasksQueue) {
        this.scannedTasksQueue = scannedTasksQueue;
        this.createdTasksQueue = createdTasksQueue;
    }

    public void invoke() {
        startTaskCreation(scannedTasksQueue, createdTasksQueue);
    }

    private void startTaskCreation(BlockingQueue<Path> scannedTasksQueue, BlockingQueue<SpiralisTask> createdTasksQueue) {
        // --------
        final ExecutorService processingExecutorService = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 8; i++) {
            final Future<Integer> processingFuture = processingExecutorService.submit(createProcessTask(scannedTasksQueue, createdTasksQueue));
        }
    }

    private Callable<Integer> createProcessTask(BlockingQueue<Path> scannedTasksQueue, BlockingQueue<SpiralisTask> createdTasksQueue) {
        return new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                for (; ; ) {
                    final Path path = scannedTasksQueue.take();
                    LOGGER.debug("Processing " + path);
                    createdTasksQueue.put(new SpiralisTask(path));
                }
            }
        };
    }
}
