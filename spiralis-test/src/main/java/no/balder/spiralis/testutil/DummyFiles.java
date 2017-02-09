package no.balder.spiralis.testutil;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 15.46
 */
public class DummyFiles {

    public static final String SAMPLE_UUID = "17524837-551a-4316-b3a3-feb9ebd84ac0";

    public static final Logger LOGGER = LoggerFactory.getLogger(DummyFiles.class);

    /**
     * Copies the -doc.xml, -rcpt.smime and -rem.xml file from the resources/ directory into the new
     * temporary test directory.
     *
     * @return
     * @throws IOException
     */
    public static Path createInboundDummyFilesInRootWithSubdirs(String... subPaths) throws IOException {

        // Creates the root directory
        Path root = Files.createTempDirectory("test");

        Path resultPath = root;
        for (String subPath : subPaths) {
            resultPath = resultPath.resolve(subPath);
            Files.createDirectories(resultPath);
        }

        for (String resourceName : sampleResourceNames()) {

            final InputStream resourceAsStream = DummyFiles.class.getClassLoader().getResourceAsStream(resourceName);

            Path file = resultPath.resolve(resourceName);

            Files.copy(resourceAsStream, file);
            LOGGER.debug("Created " + file);

        }
        LOGGER.debug("Root dir returned as " + root);
        return root;
    }


    public static List<String> sampleResourceNames() {

        return Stream.of(WellKnownFileTypeSuffix.knownValues())
                .map(e -> SAMPLE_UUID + e.getSuffix())
                .collect(Collectors.toList());
    }

    /**
     * List all files in supplied directory, no traversal of subdirectories.
     *
     * @param rootPath
     * @return
     * @throws IOException
     */
    public static List<Path> locatePayloadFilesIn(Path rootPath) throws IOException {
        final List<Path> paths = new ArrayList<>();

        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**" + WellKnownFileTypeSuffix.PAYLOAD.getSuffix());
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(file)) {
                    paths.add(file);
                }
                return CONTINUE;
            }
        });

        return paths;
    }

    public static URL samplePayloadUrl() {
        return DummyFiles.class.getClassLoader().getResource(SAMPLE_UUID + WellKnownFileTypeSuffix.PAYLOAD.getSuffix());
    }

    public static Void removeAll(Path root) throws IOException {

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

        return null;
    }

    
}
