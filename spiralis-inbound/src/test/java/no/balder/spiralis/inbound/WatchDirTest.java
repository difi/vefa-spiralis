package no.balder.spiralis.inbound;

import no.balder.spiralis.testutil.DummyFiles;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 16.26
 */
public class WatchDirTest {


    @Test
    public void testProcessEvents() throws Exception {

        final Path path = DummyFiles.createInboundDummyFilesInRootWithSubdirs();

        final BlockingQueue<Path> queue = new LinkedBlockingQueue<>();

        final AtomicLong counter = new AtomicLong(0);

        final WatchDir watchDir = new WatchDir(path, true, "glob:**-doc.xml", queue, counter);

        final Thread thread = new Thread(() -> {
            watchDir.processEvents();
        });
        thread.start();


        final Path test = Files.createTempFile(path, "test", "-doc.xml");

        long start = System.currentTimeMillis();
        do {
                     Thread.sleep(1000);
        } while (counter.get() == 0 && System.currentTimeMillis() < start + 20000);

        assertEquals(counter.get(), 1);

    }

}