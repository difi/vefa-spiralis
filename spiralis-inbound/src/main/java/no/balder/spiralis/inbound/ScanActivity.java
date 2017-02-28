package no.balder.spiralis.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Scans a root directory for all existing files and all files being added.
 * 
 */
class ScanActivity {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScanActivity.class);

    private final Path dirPath;
    private final String fileMatchGlob;
    private final BlockingQueue<Path> scannedTasksQueue;
    private ExecutorService executorService;
    private List<Future<Void>> futures = new ArrayList<>();

    // Holds number of scanned files so far.
    private AtomicLong processedCounter = new AtomicLong(0);

    public ScanActivity(Path dirPath, String fileMatchGlob, BlockingQueue<Path> scannedTasksQueue) {
        this.dirPath = dirPath;
        this.fileMatchGlob = fileMatchGlob;
        this.scannedTasksQueue = scannedTasksQueue;

        LOGGER.info("Scanning for files matching ==> " + fileMatchGlob);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public List<Future<Void>> getFutures() {
        return futures;
    }

    public void startThreads() throws InterruptedException {

        startTaskScanning(scannedTasksQueue);
    }

    /**
     * Starts the scanning of the given directory tree.
     *
     * @param scannedTasksQueue
     * @throws InterruptedException
     */
    private void startTaskScanning(BlockingQueue<Path> scannedTasksQueue) throws InterruptedException {

        executorService = Executors.newFixedThreadPool(2);

        // Starts the watcher first in order to ensure that we notice changes made while scannig for existing files.

        // Starts the payload watcher and the payload scanner
        final Future<Void> watchDirFuture = executorService.submit(createWatchDirTask());
        futures.add(watchDirFuture);

        // Starts the scanner of existing files
        CountDownLatch findExistingFilesLatch = new CountDownLatch(1);
        final Future<Void> findExistingFilesFuture = executorService.submit(createFindExistingFilesTask(dirPath, fileMatchGlob,scannedTasksQueue, findExistingFilesLatch));
        futures.add(findExistingFilesFuture);
    }


    private Callable<Void> createWatchDirTask() {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                new WatchDir(dirPath, true, fileMatchGlob, scannedTasksQueue, processedCounter).processEvents();
                return null;
            }
        };
    }

    private Callable<Void> createFindExistingFilesTask(Path scanDirectoryPath, String fileMatchGlob, BlockingQueue<Path> q, final CountDownLatch countDownLatch) {
        return new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                FileFinder fileFinder = new FileFinder(scanDirectoryPath, fileMatchGlob, q);
                // Visits all directories and files
                final int fileCount = fileFinder.findFiles();

                LOGGER.debug("Found " + fileCount + " existing files");

                // Signals completion
                countDownLatch.countDown();

                // Updates the number of files processed
                processedCounter.addAndGet(fileCount);
                return null;
            }
        };
    }

    public Long getProcessedCounter() {
        return processedCounter.get();
    }
}