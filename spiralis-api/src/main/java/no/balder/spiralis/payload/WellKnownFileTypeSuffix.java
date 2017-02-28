package no.balder.spiralis.payload;

import com.sun.org.apache.regexp.internal.RE;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a collection of file name suffixes to be found for each transmission
 *
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 15.40
 */
public enum WellKnownFileTypeSuffix {

    PAYLOAD(".doc.xml"),
    AS2_RECEIPT(".receipt.smime"),
    META_JSON(".meta.json"),
    REM_EVIDENCE(".receipt.dat"),
    UNKNOWN(".unknown.unknown");

    private final String suffix;

    WellKnownFileTypeSuffix(String suffix) {

        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * Creates a Path matching glob which should match across directory boundaries i.e. x/y/z
     * 
     * @return
     */
    public static String globOfAllTypesInSubdirs() {
        final String listOfTypes = Stream.of(values())
                .filter(suffix -> suffix != UNKNOWN)
                .map(WellKnownFileTypeSuffix::getSuffix)
                .collect(Collectors.joining(","));
        final String glob = "glob:**{" + listOfTypes + "}";
        return glob;
    }

    /**
     * Provides a file system glob crossing directory boundaries for this suffix.
     * @return a complete file system glob to be used by the
     */
    public String glob(){
        return "glob:**{" + suffix + "}";
    }
}
