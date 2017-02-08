package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;

import java.nio.file.Path;
import java.util.Optional;

/**
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 19.02
 */
class PayloadClassifier {


    static WellKnownFileTypeSuffix classify(Path path) {
        
        for (WellKnownFileTypeSuffix wellKnownFileTypeSuffix : WellKnownFileTypeSuffix.values()) {

            final Path fileName = path.getFileName();
            if (fileName.toString().endsWith(wellKnownFileTypeSuffix.getSuffix()))
                return wellKnownFileTypeSuffix;
        }

        return WellKnownFileTypeSuffix.UNKNOWN;
    }
}
