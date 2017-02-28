package no.balder.spiralis.payload;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Utility methods for working the Path objects of payload files, AS2 MDN files, etc.
 *
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 19.02
 */
public class ReceptionPathUtil {


    // TODO: Externalize the regexp for file name suffixes
    static Pattern baseFilePattern = Pattern.compile("(.*)\\.\\w+\\.\\w+$");

    /**
     * Classify a file according to the suffix and file type.
     * I.e. the suffix is typically something like {@code .doc.xml}
     *
     * @param path
     * @return
     */
    public static WellKnownFileTypeSuffix classify(Path path) {

        for (WellKnownFileTypeSuffix wellKnownFileTypeSuffix : WellKnownFileTypeSuffix.values()) {

            final Path fileName = path.getFileName();
            if (fileName.toString().endsWith(wellKnownFileTypeSuffix.getSuffix()))
                return wellKnownFileTypeSuffix;
        }

        return WellKnownFileTypeSuffix.UNKNOWN;
    }

    public static List<Path> associatedFiles(Path path) {
        final String fileNameBodyPart = fileNameBodyPart(path);

        final Path parent = path.getParent();
        final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

            @Override
            public boolean accept(Path entry) throws IOException {
                if (entry.getFileName().toString().startsWith(fileNameBodyPart)) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent, filter)) {
            return StreamSupport.stream(stream.spliterator(), false).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Error while inspecting " + parent);
        }
    }

    /**
     * Creates a new target path for a file within a base dir.
     * <p>
     * Given {@code /root/src/foo/bar.txt}, srcBasedir of {@code /root/src/} and
     * a destination path {@code /root/dest}, this method will return
     * {@code /root/dest/foo/bar.txt}
     * <p>
     * Usefull when moving an entire structure from one place to another
     *
     * @param srcPathSegment    the root path segment of the filename
     * @param targetPathSegment the target path segment
     * @param filePath          the full name of the existing file path to be taken as input
     * @return complete path of file in the destination path
     */
    public static Path createNewPathFor(Path srcPathSegment, Path filePath, Path targetPathSegment) {

        if (!srcPathSegment.isAbsolute()) {
            throw new IllegalStateException("Src root path must be absolute:" + srcPathSegment);
        }
        if (!targetPathSegment.isAbsolute()) {
            throw new IllegalArgumentException("Destination path must be absolute" + targetPathSegment);
        }
        // Verifies that file is absolute and embedded in srcPathSegment
        if (!filePath.isAbsolute()) {
            throw new IllegalArgumentException("File path must be absolute: " + filePath);
        }

        if (!filePath.startsWith(srcPathSegment)) {
            throw new InvalidPathException("File path must be within the src root path", filePath + " does not start with " + srcPathSegment);
        }

        final Path relativized = srcPathSegment.relativize(filePath);
        final Path resolved = targetPathSegment.resolve(relativized);
        return resolved;
    }

    public static String fileNameBodyPart(Path path) {
        final String fileName = path.getFileName().toString();
        Matcher m = baseFilePattern.matcher(fileName);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static Path move(Path srcPath, Path targetPath) {
        if (!srcPath.isAbsolute() || !targetPath.isAbsolute()) {
            throw new InvalidPathException("Both paths must be absolute", "Neither " + srcPath + " nor " + targetPath + " is absolute");
        }
        try {
            final Path directories = Files.createDirectories(targetPath.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create path " + targetPath);
        }
        try {
            final Path moved = Files.move(srcPath, targetPath);
            return moved;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to move " + srcPath + " to " + targetPath);
        }

    }


    public static Path moveWithSubdirIntact(Path inboundRoot, Path completeFilePath, Path destinationDir) {
        if (!completeFilePath.isAbsolute()) {
            throw new IllegalStateException("Source file must be absolute");
        }

        // Computes path in archive directory...
        final Path newPathFor = ReceptionPathUtil.createNewPathFor(inboundRoot, completeFilePath, destinationDir);

        // Performs the actual move
        final Path moved = ReceptionPathUtil.move(completeFilePath, newPathFor);
        return moved;
    }

    /**
     * Returns true one path references same directory tree.
     * <p>
     * {@code /tmp/A != /tmp/B} while {@code /tmp/A/B == /tmp/A/B/C}
     *
     * @param p1
     * @param p2
     * @return
     */
    public static boolean overlaps(Path p1, Path p2) {
        if (p1.equals(p2)) {
            return true;
        }
        if (p1.getNameCount() < p2.getNameCount()) {
            return p2.startsWith(p1);
        } else if (p1.getNameCount() > p2.getNameCount()) {
            return p1.startsWith(p2);
        }

        return false;
    }

}
