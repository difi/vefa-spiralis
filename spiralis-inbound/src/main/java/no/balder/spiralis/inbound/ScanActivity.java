package no.balder.spiralis.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.*;

class ScanActivity {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScanActivity.class);

    private final Path dirPath;
    private final BlockingQueue<Path> scannedTasksQueue;

    public ScanActivity(Path dirPath, BlockingQueue<Path> scannedTasksQueue) {
        this.dirPath = dirPath;
        this.scannedTasksQueue = scannedTasksQueue;
    }

    public Future<Void> invoke() throws InterruptedException {
        return startTaskScanning(scannedTasksQueue);
    }

    private Future<Void> startTaskScanning(BlockingQueue<Path> scannedTasksQueue) throws InterruptedException {
        final ExecutorService taskScannerExecutorService = Executors.newFixedThreadPool(2);

        // Starts the watcher first in order to ensure that we notice changes made while scannig for existing files.

        // Starts the payload watcher and the payload scanner
        final Future<Void> watchDirFuture = taskScannerExecutorService.submit(createWatchDirTask());

        CountDownLatch findExistingFilesLatch = new CountDownLatch(1);
        final Future<Integer> findExistingFilesFuture = taskScannerExecutorService.submit(createFindExistingFilesTask(dirPath, scannedTasksQueue, findExistingFilesLatch));


        try {
            final Integer existingFilesCount = findExistingFilesFuture.get();
            LOGGER.debug("Found " + existingFilesCount + " existing files");
        } catch (ExecutionException e) {
            throw new IllegalStateException("Unable to find all existing files in " + dirPath + "; " + e.getMessage(), e);
        }
        return watchDirFuture;
    }

    private Callable<Void> createWatchDirTask() {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                new WatchDir(dirPath, true, scannedTasksQueue).processEvents();
                return null;
            }
        };
    }

    private Callable<Integer> createFindExistingFilesTask(Path scanDirectoryPath, BlockingQueue<Path> q, final CountDownLatch countDownLatch) {
        return new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {

                FileFinder fileFinder = new FileFinder(scanDirectoryPath, FileFinder.GLOB_XML, q);
                // Visits all directories and files
                final int fileCount = fileFinder.findFiles();

                LOGGER.debug("Found " + fileCount + " existing files");

                // Signals completion
                countDownLatch.countDown();
                return fileCount;
            }
        };
    }
}