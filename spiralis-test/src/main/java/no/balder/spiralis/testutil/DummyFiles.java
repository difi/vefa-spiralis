package no.balder.spiralis.testutil;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 15.46
 */
public class DummyFiles {

    public static final String SAMPLE_UUID = "17524837-551a-4316-b3a3-feb9ebd84ac0";

    /**
     * Copies the -doc.xml, -rcpt.smime and -rem.xml file from the resources/ directory into the new
     * temporary test directory.
     * @return
     * @throws IOException
     */
    public static Path createInboundDummyFiles() throws IOException {

        // Creates the root directory
        Path root = Files.createTempDirectory("test");

        // Creates root/inbound
        Path resultPath = root.resolve("inbound");
        Files.createDirectories(resultPath);

        for (WellKnownFileTypeSuffix suffix : WellKnownFileTypeSuffix.values()) {
            final String resourceName = SAMPLE_UUID + suffix.getSuffix();

            final InputStream resourceAsStream = DummyFiles.class.getClassLoader().getResourceAsStream(resourceName);

            Path file = resultPath.resolve(resourceName);

            Files.copy(resourceAsStream, file);
        }
        return root;
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
