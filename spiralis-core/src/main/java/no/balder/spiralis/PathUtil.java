package no.balder.spiralis;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author steinar
 *         Date: 15.12.2016
 *         Time: 15.45
 */
public class PathUtil {

    static Pattern p = Pattern.compile("(.*)(\\.\\w+)$");

    public static Path replaceExtensionWith(Path path, String newExtension) {

        Matcher matcher = p.matcher(path.getFileName().toString());
        boolean matches = matcher.matches();
        if (!matches) {
            throw new IllegalStateException("Unable to find extension for file name " + path.getFileName());
        }

        String baseFile = matcher.group(1);
        String extension = matcher.group(2);

        String newFileName = baseFile + newExtension;


        Path newPath = Paths.get(path.getParent().toString(), newFileName);

        return newPath;
    }
}
