package no.balder.spiralis.inbound;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 16.42
 */
class TaskScanner implements Iterable<InboundTask>{


    private final Path scanPath;

    @Inject
    public TaskScanner(Path scanPath) {

        this.scanPath = scanPath;
    }

    @Override
    public Iterator<InboundTask> iterator() {

        return new TaskIterator();

    }


    private class TaskIterator implements Iterator<InboundTask> {


        private final Iterator<Path> iterator;

        public TaskIterator() {

            try {
                DirectoryStream<Path> stream = Files.newDirectoryStream(scanPath);
                iterator = stream.iterator();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create directory scanner");
            }
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public InboundTask next() {
            Path nextPath = iterator.next();

            return new InboundTask(nextPath);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can not remove payload file from ");

        }
    }
}
