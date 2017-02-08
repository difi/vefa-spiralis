package no.balder.spiralis.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author steinar
 *         Date: 04.02.2017
 *         Time: 12.18
 */
class CreateTaskActivity {

    public static final Logger LOGGER = LoggerFactory.getLogger(CreateTaskActivity.class);
    public static final int N_THREADS = 4;

    private final BlockingQueue<Path> scannedTasksQueue;
    private final BlockingQueue<SpiralisTask> createdTasksQueue;
    private ExecutorService processingExecutorService;
    private int threadNumber=0;
    private AtomicLong processCount = new AtomicLong(0);

    public CreateTaskActivity(BlockingQueue<Path> scannedTasksQueue, BlockingQueue<SpiralisTask> createdTasksQueue) {
        this.scannedTasksQueue = scannedTasksQueue;
        this.createdTasksQueue = createdTasksQueue;
    }

    public void invoke() {
        startTaskCreation(scannedTasksQueue, createdTasksQueue);
    }


    /**
     * Creates {@link ExecutorService} with N_THREADS, which will execute N_THREADS ({@link Callable}) instances.
     * The {@link Callable} instances will read from a queue, createInboundBlobName tasks (work order requests) and place them on
     * the other queue.
     * @param scannedTasksQueue
     * @param createdTasksQueue
     */
    private void startTaskCreation(BlockingQueue<Path> scannedTasksQueue, BlockingQueue<SpiralisTask> createdTasksQueue) {

        processingExecutorService = Executors.newFixedThreadPool(N_THREADS);

        // Every thread in the ExecutorService gets to execute a Callable worker.
        for (int i = 0; i < N_THREADS; i++) {
            final Future<Long> processingFuture = processingExecutorService.submit(createProcessTask(scannedTasksQueue, createdTasksQueue));
        }
    }


    /**
     * Creates  {@link Callable} instances of task which will execute the following loop:
     * <ol>
     *     <li>take {@link Path} instance from scanned tasks queue</li>
     *     <li>Create {@link SpiralisTask} instance</li>
     *     <li>Place instance of {@link SpiralisTask} on created tasks queue</li>
     * </ol>
     * @param scannedTasksQueue queue from which Path instances are to be retrieved (taken)
     * @param createdTasksQueue queue into which created tasks should be put
     * @return
     */
    private Callable<Long> createProcessTask(BlockingQueue<Path> scannedTasksQueue, BlockingQueue<SpiralisTask> createdTasksQueue) {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                for (; ; ) {
                    final Path path = scannedTasksQueue.take();
                    LOGGER.debug("Processing " + path);

                    // Inspects the payload type based upon the filename suffix
                    try {

                        final SpiralisTask spiralisTask = SpiralisTaskFactory.insepctInbound(path);
                        createdTasksQueue.put(spiralisTask);
                        processCount.incrementAndGet();
                        LOGGER.debug(path + " placed on queue");
                    } catch (InterruptedException e) {
                        LOGGER.warn("Interrupted, returning with " + processCount.get() + " paths processed");
                        return processCount.get();
                    } catch (Exception e) {
                        LOGGER.warn("Unable to process " + path + ", reason:" + e.getMessage(), e);
                        LOGGER.info("Continuing processing");
                        continue;
                    }
                }
            }
        };
    }

    public Long getProcessedCounter() {
        return processCount.get();
    }
}
