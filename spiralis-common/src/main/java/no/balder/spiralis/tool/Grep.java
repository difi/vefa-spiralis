package no.balder.spiralis.tool;
/*
 * Copyright (c) 2001, 2014, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/* Search a list of files for lines that match a given regular-expression
 * pattern.  Demonstrates NIO mapped byte buffers, charsets, and regular
 * expressions.
 */

public class Grep {


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
