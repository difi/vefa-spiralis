package no.balder.spiralis.payload;

import com.sun.org.apache.regexp.internal.RE;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 15.40
 */
public enum WellKnownFileTypeSuffix {

    PAYLOAD("-doc.xml"),
    AS2_RECEIPT("-rcpt.smime"),
    REM_EVIDENCE("-rem.xml"),
    UNKNOWN("-unknown.unknown");

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
        final String listOfTypes = Stream.of(PAYLOAD, AS2_RECEIPT, REM_EVIDENCE)
                .map(WellKnownFileTypeSuffix::getSuffix)
                .collect(Collectors.joining(","));
        final String glob = "glob:**{" + listOfTypes + "}";
        return glob;
    }

    public static WellKnownFileTypeSuffix[] knownValues() {
        return new WellKnownFileTypeSuffix[] {PAYLOAD, AS2_RECEIPT, REM_EVIDENCE};
    }
}
