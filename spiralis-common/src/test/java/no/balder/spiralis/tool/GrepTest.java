package no.balder.spiralis.tool;

import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 09.12
 */
public class GrepTest {

    @Test
    public void testGrep() throws Exception {

        final URL resource = GrepTest.class.getClassLoader().getResource("sample-smime-file.txt");
        final Pattern pattern = Pattern.compile("^message-id\\h*:\\h*(.*)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);


        Path path = Paths.get(resource.toURI());
        long start = System.nanoTime();
        File file = path.toFile();

        final List<String> strings = Grep.grep(file, pattern, 0);
        //final List<String> strings = performGrep(file, pattern, 1);

        long elapsed = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        assertTrue(strings.size() == 2);

        System.out.println(strings);
        System.out.println(elapsed + "ms");
    }

    List<String> performGrep(File f, Pattern pattern, int group) throws IOException {


        // Open the file and then get a channel from the stream
        try (FileInputStream fis = new FileInputStream(f); FileChannel fc = fis.getChannel()) {

            List<String> result = new ArrayList<>();
            // Get the file's size and then map it into memory
            int sz = (int) fc.size();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

            Charset charset = Charset.forName("UTF-8");
            CharsetDecoder decoder = charset.newDecoder();

            // Decode the file into a char buffer
            CharBuffer cb = decoder.decode(bb);

            // Perform the search
            final Matcher matcher = pattern.matcher(cb);

            while (matcher.find()) {
                result.add(matcher.group(1));
            }
            return result;
        }
    }


}