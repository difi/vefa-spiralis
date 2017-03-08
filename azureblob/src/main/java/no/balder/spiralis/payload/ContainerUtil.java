package no.balder.spiralis.payload;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 17.53
 */
class ContainerUtil {


    /**
     * Computes the name of the container into which this file should be uploaded.
     * <p>
     * The base file name used as input is the file name only, less any extension, i.e.
     * <pre>
     *         /var/peppol/IN/9908_971589671/9908_848382922/2017-01-10/17524837-551a-4316-b3a3-feb9ebd84ac0-doc.xml
     *     </pre>
     * </p>
     *
     * @param path
     * @return
     */
    static String containerNameFor(Path path) {
        return "peppol-ap";
    }

    /**
     * @param path
     * @return
     */
    static String getBaseFileNameOnly(Path path) {
        // Extracts the filename part
        final Path fileName = path.getFileName();

        // Strips of the extension
        final Pattern pattern = Pattern.compile("(.*)\\.(receipt.smime|doc.xml|receipt.dat|meta.json)$");  // TODO: externalize the regexp
        final Matcher matcher = pattern.matcher(fileName.toString());
        if (matcher.find()) {
            final String baseFileName = matcher.group(1);
            return baseFileName;
        } else
            throw new IllegalArgumentException("Unable to determine basic filename of " + path);

    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    private static long bytesToLong(byte[] digest) {
        return bytesToLong(digest, 8);  // Long is 64 bits (8 bytes)
    }

    public static long bytesToLong(byte[] b, int n_bytes) {
        long result = 0;
        for (int i = 0; i < n_bytes; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}
