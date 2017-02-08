package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.tool.Grep;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.sbdh.lang.SbdhException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.difi.vefa.peppol.common.model.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inspects a file reference by a supplied {@link Path}, figures out various meta data properties and
 * creates a {@link SpiralisTask}
 * <p>
 *     In future releases, this class should be refactored to handle other protocols than AS2.
 * </p>
 * 
 * @author steinar
 *         Date: 06.02.2017
 *         Time: 19.26
 */
class SpiralisTaskFactory {

    public static final Logger LOGGER = LoggerFactory.getLogger(SpiralisTaskFactory.class);

    static Pattern baseFilePattern = Pattern.compile("(.*)" + WellKnownFileTypeSuffix.PAYLOAD.getSuffix());

    static SpiralisTask insepctInbound(Path path) {
        if (!path.getFileName().toString().endsWith(WellKnownFileTypeSuffix.PAYLOAD.getSuffix())) {
            throw new IllegalArgumentException("Can not process " + path + ", unknown type");
        }

        Header header;
        // Parses the SBDH
        try (final SbdReader sbdReader = SbdReader.newInstance(Files.newInputStream(path))) {

            // Extracts the SBDH from the input file
            header = sbdReader.getHeader();
        } catch (SbdhException e) {
            throw new IllegalStateException("Unable to retrieve SBDH from " + path + ", reason:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to process " + path + ", reason: " + e.getMessage(), e);
        }

        final SpiralisTask spiralisTask = new SpiralisTask(path, header);


        // See if we can find the S/MIME file
        final String baseFileName = SpiralisTaskFactory.baseFileName(path);

        // Base file name is expected to hold the UUID assigned by us upon reception.
        spiralisTask.setOurMessageId(baseFileName);

        final Path smimePath = path.getParent().resolve(baseFileName + WellKnownFileTypeSuffix.AS2_RECEIPT.getSuffix());
        if (Files.exists(smimePath)) {

            spiralisTask.setSmimePath(smimePath);

            // Attempts to locate the AS2 Message-ID
            final Pattern pattern = Pattern.compile("^message-id\\h*:\\h*(.*)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            final List<String> grepResult = Grep.grep(smimePath.toFile(), pattern, 1);
            // We grab the last one found
            final String as2MessageId = grepResult.get(grepResult.size() - 1);
            spiralisTask.setTransmissionId(as2MessageId);
        } else
            LOGGER.warn("S/MIME file {} not found", smimePath);

        // Use the timestamp of payload file creation, assuming it was not moved anywhere
        try {
            final BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
            final FileTime fileTime = basicFileAttributes.creationTime();
            final Instant instant = Instant.ofEpochMilli(fileTime.toMillis());
            final OffsetDateTime creationOffsetDateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());

            spiralisTask.setReceived(creationOffsetDateTime);

        } catch (IOException e) {
            spiralisTask.setReceived(OffsetDateTime.now());
            LOGGER.warn("Unable to obtain basicFileAttributes for " + path + "; " + e.getMessage() + " using " + spiralisTask.getReceived(), e);
        }

        return spiralisTask;
    }

    static String baseFileName(Path path) {
        // Extracts the UUID part of the filename, i.e. the suffix is discarded
        final String s = path.getFileName().toString();

        final Matcher matcher = baseFilePattern.matcher(s);
        if (!matcher.find()) {
            throw new IllegalStateException("Unable to determine base filename for " + s);
        }
        final String group = matcher.group(1);

        return group;
    }
}
