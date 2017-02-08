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

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 09.12
 */
public class GrepTest {

    @Test
    public void testGrep() throws Exception {

        final Pattern pattern = Pattern.compile("^message-id\\h*:\\h*(.*)$", CASE_INSENSITIVE | MULTILINE);

        final URL resource = sampleFileUrl();

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

    private URL sampleFileUrl() {
        return GrepTest.class.getClassLoader().getResource("sample-smime-file.txt");
    }

    @Test
    public void findOriginalMessageID() throws Exception {

        final Grep grep = new Grep(new File(sampleFileUrl().toURI()));

        final Pattern pattern = Pattern.compile("Original-Message-ID\\h*:\\h*(.*)$", CASE_INSENSITIVE | MULTILINE);
        String msgId = grep.grepFirstGroup(pattern);

        assertNotNull(msgId);
        assertEquals(msgId, "17524837-551a-4316-b3a3-feb9ebd84ac0");

        
        String rcptId = grep.grepFirstGroup(Pattern.compile("Original-Recipient\\h*:\\h*rfc822\\h*;\\h*(.*)$", CASE_INSENSITIVE | MULTILINE));
        assertNotNull(rcptId);
        assertEquals(rcptId, "APP_1000000146");
    }
}