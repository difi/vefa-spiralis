package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.testutil.DummyFiles;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 16.17
 */
public class ScanActivityTest {

    /**
     * <ol>
     *     <li>Creates some test files</li>
     *     <li>Verifies that they have been found by the {@link ScanActivity} instance</li>
     *     <li>Creates another file</li>
     *     <li>Verifies that the additional file has been found as well</li>
     * </ol>
     * @throws Exception
     */
    @Test
    public void testStartThreads() throws Exception {

        // Creates dummy files
        final Path testRootPath = DummyFiles.createInboundDummyFilesInRootWithOptionalSubdirs();

        // Creates the ScanActivity instance
        BlockingQueue<Path> queue = new LinkedBlockingQueue<>();
        final ScanActivity scanActivity = new ScanActivity(testRootPath, WellKnownFileTypeSuffix.globOfAllTypesInSubdirs(), queue);
        scanActivity.startThreads();    // Starts the threads

        // Waits for 1000ms to ensure that all three files have been picked up
        long scanned = 0;
        long start = System.currentTimeMillis();
        do {
            Thread.sleep(1000); // Gives the ScanActivity the chance to find the files.
            scanned = scanActivity.getProcessedCounter();
        } while (scanned < 3 && System.currentTimeMillis() < start + 1000);

        // In case we timed out of the loop, verify that we actually found 3 items.
        assertEquals(queue.size(), 4);

        // Writes another dummy file
        Files.write(testRootPath.resolve("dummy-doc.xml"), "rubbish".getBytes());

        //
        start = System.currentTimeMillis();
        do {

        } while (queue.size() < 4 && System.currentTimeMillis() < start + 10000);

        assertEquals(queue.size(), 4,"Did not find all files as expected");
    }

}