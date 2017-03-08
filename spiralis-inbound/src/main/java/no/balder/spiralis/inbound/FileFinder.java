package no.balder.spiralis.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Waks a file tree, looking for all
 *
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 18.00
 */
class FileFinder extends SimpleFileVisitor<Path> {

    public static final Logger LOGGER = LoggerFactory.getLogger(FileFinder.class);


    private final Path root;
    private final BlockingQueue<Path> queue;
    private PathMatcher pathMatcher;

    private int count = 0;

    public FileFinder(Path root, String pattern, BlockingQueue<Path> queue) {

        this.root = root;
        String pattern1 = pattern;
        this.queue = queue;

        pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
    }


    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (pathMatcher.matches(file)) {
            if (!queue.contains(file)) {
                try {
                    queue.put(file);
                    LOGGER.debug("Added " + file.toString() + " to the queue");
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Unable to place " + file + " on queue",e);
                }
                count++;
            }
        }
        return CONTINUE;
    }

    int findFiles() {
        try {
            Files.walkFileTree(root, this);
            return count;
        } catch (IOException e) {
            throw new IllegalStateException("Walking the file tree failed: " + e.getMessage(), e);
        }
    }
}
