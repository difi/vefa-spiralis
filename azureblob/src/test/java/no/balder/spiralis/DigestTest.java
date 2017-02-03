package no.balder.spiralis;

import org.testng.annotations.Test;

import java.security.MessageDigest;
import java.util.Base64;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 02.02.2017
 *         Time: 16.16
 */
public class DigestTest {

    String[] inputs = {
            "134f84fc-50b9-41d9-a77f-854518bbbccf", "1ee699a3-5397-452b-bb12-f1e83c2e3856",
            "323b0451-1413-44fd-99fe-b911898cab5f", "49cb4757-144e-4e77-af65-157e6e311588", "4a03a5d7-9dc7-4baf-8797-f6da0f951997"};

    String[] digests = {
            "JJTl7DL8jerIjjE/bSqKXsgk8Nk=", "4/ZEX+Fa7mS8dUrviIaTDOmdE7M=",
            "qFtebF+BLq2R8nclO1Qo7hnte50=",
            "HOyJyzzqIefdS8jIPSLC5itXHTU=",
            "3zEBnNMFu935/RW8wyASfs6L+NI="
    };

    @Test
    public void testDigest() throws Exception {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        System.out.printf("%-36s  %-30s  %-20s  %-20s %7s\n", "inndata", "SHA-1 i base64", "Long", "uLong (56bit)","Mod29");
        for (int i = 0; i < inputs.length; i++) {
            byte[] digest = sha1.digest(inputs[i].getBytes());
            byte[] encode = Base64.getEncoder().encode(digest);
            String s = new String(encode);

            assertEquals(s, digests[i]);

            long l = bytesToLong(digest);

            long ulong = l & 0x00ffffffffffffffL;
            System.out.printf("%-36s  %-30s  %20d  %20d %7d %s %s\n", inputs[i], digests[i], l, ulong, (ulong % 29), Long.toHexString(l), Long.toHexString(ulong));
        }

    }

    @Test
    public void testBitStuff() throws Exception {

        byte[] bytes = { -0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        long l = bytesToLong(bytes);
        System.out.println(l);

    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}
