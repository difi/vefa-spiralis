package no.balder.spiralis.payload;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 17.53
 */
class ContainerUtil {


    /**
     * Computes the name of the container into which this file should be uploaded by digesting
     * the filename part of the path, digesting with SHA-1, creating a long from the first 7 bytes
     * which is finally modulus calculated.
     *
     * @param path
     * @return
     */
    static String containerNameFor(Path path) {

        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");  // Not thread safe
            String filenameOnly = getFileNameOnly(path);


            final byte[] digest = sha1.digest(filenameOnly.getBytes());

            long l = bytesToLong(digest,7);
            l = (l % 29);

            return String.format("peppol-ap-%02d",(int)l);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 not supported in this Java runtime. " + e.getMessage(), e);
        }
    }

    static String getFileNameOnly(Path path) {
        // Extracts the filename part
        final Path fileName = path.getFileName();
        return fileName.toString();
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
