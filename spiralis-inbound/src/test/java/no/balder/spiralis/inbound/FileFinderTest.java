package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.testutil.DummyFiles;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 18.20
 */
public class FileFinderTest {

    /**
     * Creates
     * <pre>
     *  root/inbound/dummy.xml
     *  root/inbound/dummy-doc.xml
     *  root/inbound/dummy-rcpt.xml
     *  root/inbound/dummy-rcpt.smime
     * </pre>
     * @throws Exception
     */
    @Test
    public void testFindFiles() throws Exception {

        Path root = DummyFiles.createInboundDummyFilesInRootWithSubdirs();

        BlockingQueue<Path> queue = new LinkedBlockingDeque<>();

        // Locates the files matching the supplied glob and places them into the supplied queue
        // Should match -doc.xml, -rem.xml and -rcpt.smime
        FileFinder fileFinder = new FileFinder(root, WellKnownFileTypeSuffix.globOfAllTypesInSubdirs(), queue);
        fileFinder.findFiles();

        assertEquals(queue.size(),3);

        // Clean up
        DummyFiles.removeAll(root);
    }
}