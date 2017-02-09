package no.balder.spiralis.payload;

import no.balder.spiralis.testutil.DummyFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author steinar
 *         Date: 08.02.2017
 *         Time: 20.13
 */
public class RenamePayloadTest {

    @Test
    public void testRename() throws Exception {

        final Path inboundDummyFiles = DummyFiles.createInboundDummyFilesInRootWithSubdirs();
        final PayloadRenamer visitor = new PayloadRenamer();
        Files.walkFileTree(Paths.get("/var","peppol","IN"), visitor);
        final Map<String, List<Path>> pathsByBaseName = visitor.getPathsByBaseName();

        for (Map.Entry<String, List<Path>> entry : pathsByBaseName.entrySet()) {
            System.out.println(entry.getKey());
            final List<Path> pathList = entry.getValue();
            for (Path path : pathList) {
                System.out.println("\t" +path);
            }
        }

    }

    static class PayloadRenamer extends SimpleFileVisitor<Path> {

        public static final Logger LOGGER = LoggerFactory.getLogger(PayloadRenamer.class);

        private final PathMatcher pathMatcher;
        Map<String, List<Path>> pathsByBaseName = new HashMap<>();

        public PayloadRenamer() {
            // Matches all -*.* files
            pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**"+WellKnownFileTypeSuffix.PAYLOAD.getSuffix());
        }

        public Map<String, List<Path>> getPathsByBaseName() {
            return pathsByBaseName;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            if (pathMatcher.matches(path)) {
                final String fileNameBody = PayloadPathUtil.fileNameBodyPart(path);
                addToMap(fileNameBody, path);
            } else
                LOGGER.debug("Skipping " + path);

            return FileVisitResult.CONTINUE;
        }

        private void addToMap(String fileNameBody, Path path) {
            List<Path> paths;
            if (pathsByBaseName.containsKey(fileNameBody)) {
                // Obtains reference of existing list
                paths= pathsByBaseName.get(fileNameBody);
            } else {
                // Creates new list
                paths = new ArrayList<>();
                pathsByBaseName.put(fileNameBody,paths);
            }

            // Adds the new entry
            paths.add(path);

            try {
                final DirectoryStream<Path> stream = Files.newDirectoryStream(path.getParent(), "*"+fileNameBody+"-*.*");
                stream.forEach(p -> {
                    if (!paths.contains(p)){
                        paths.add(p);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return super.visitFileFailed(file, exc);
        }
    }
}
