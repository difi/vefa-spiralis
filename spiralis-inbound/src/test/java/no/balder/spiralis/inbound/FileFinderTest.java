package no.balder.spiralis.inbound;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 18.20
 */
public class FileFinderTest {

    @Test
    public void testFindFiles() throws Exception {


        Path root = Files.createTempDirectory("test");
        Path inbound = root.resolve("inbound");
        Files.createDirectories(inbound);

        Path dummyFile = inbound.resolve("dummy.xml");

        Files.write(dummyFile, "<xml>dummy</xml>".getBytes());

        BlockingQueue<Path> queue = new LinkedBlockingDeque<>();

        FileFinder fileFinder = new FileFinder(root, FileFinder.GLOB_XML, queue);
        fileFinder.findFiles();

        assertEquals(1,queue.size());

        Files.walkFileTree(root, new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }
}