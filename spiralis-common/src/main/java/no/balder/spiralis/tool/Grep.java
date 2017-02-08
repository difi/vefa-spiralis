package no.balder.spiralis.tool;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Poor mans grep utility.
 *
 * For one time searches, use the static method.
 *
 * If you are going to make several searches in the same file, use the instance methods.
 *
 * @author steinar
 */
public class Grep {


    // Holds the data in memory
    private final CharBuffer charBuffer;

    /**
     * Loads the data from the supplied file into memory, making it available for subsequent application of
     * regexp {@link Pattern} instances.
     *
     * @param file file to load
     */
    public Grep(File file) {

        try (FileInputStream fis = new FileInputStream(file); FileChannel fc = fis.getChannel()) {

            // Get the file's size and then map it into memory
            int sz = (int) fc.size();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

            final CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();

            // Decode the file into a char buffer
            charBuffer = charsetDecoder.decode(bb);

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found " + file, e);
        } catch (IOException e) {
            throw new IllegalStateException("I/O error while inspecting file " + file,e);
        }
    }

    /**
     * Provides access to the {@link CharBuffer} in case you want to perform your own magic {@link Pattern}
     * matching.
     *
     * @return reference to the in-memory representation of the file.
     */
    public CharBuffer getCharBuffer() {
        return charBuffer;
    }

    /**
     * Searches the loaded {@link CharBuffer} for given pattern, returns a list of the group id of the regexp'es matched
     *
     * @param pattern
     * @param groupIndex
     * @return
     */
    public List<String> grep(Pattern pattern, int groupIndex) {

        List<String> results = new ArrayList<>();
        Matcher matcher = pattern.matcher(charBuffer);
        while (matcher.find()) {
            if (matcher.groupCount() >= groupIndex) {
                results.add(matcher.group(groupIndex));
            }
        }
        return results;
    }

    public String grepFirstGroup(Pattern pattern) {
        Matcher m = pattern.matcher(charBuffer);
        if (m.find()) {
            return m.group(1);
        }
        
        else return null;
    }


    /**
     * Performs grep operation on a file
     * @param file the File to be searched
     * @param pattern the regexp pattern to match against
     * @param groupIndex the group index to retrieve, use 0 to retrieve entire match
     * @return list of matches
     * @throws IOException
     */
    public static List<String> grep(File file, Pattern pattern, int groupIndex)  {


        List<String> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file); FileChannel fc = fis.getChannel()) {
        //try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {

            // Get the file's size and then map it into memory
            int sz = (int) fc.size();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

            final CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();
            // Decode the file into a char buffer
            CharBuffer cb = charsetDecoder.decode(bb);

            // Performs the search
            Matcher matcher = pattern.matcher(cb);

            // Foreach match found, saveInboundTask it
            while (matcher.find()) {
                if (matcher.groupCount() >= groupIndex) {
                    result.add(matcher.group(groupIndex));
                }
            }
            return result;
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found " + file, e);
        } catch (IOException e) {
            throw new IllegalStateException("I/O error while inspecting file " + file,e);
        }
    }
}
