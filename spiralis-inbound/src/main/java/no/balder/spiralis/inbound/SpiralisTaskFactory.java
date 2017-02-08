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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.difi.vefa.peppol.common.model.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;

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

    /** RFC3798, section 3.2.5 */
    public static final Pattern messageIdPattern =
            Pattern.compile("Original-Message-ID\\h*:\\h*(.*)$", CASE_INSENSITIVE | MULTILINE);

    /**
     * RFC3798, section 3.2.4
     * <quote>
     *     The Final-Recipient field indicates the recipient for which the MDN
     *     is being issued.  This field MUST be present.
     * </quote>
     */
    public static final Pattern receiverOfMdnPattern =
            Pattern.compile("Final-Recipient\\h*:\\h*rfc822\\h*;\\h*(.*)$", CASE_INSENSITIVE | MULTILINE);


    static SpiralisTask insepctInbound(Path payloadPath) {
        // Ensures that we only process *-doc.xml files, containing payload
        if (PayloadClassifier.classify(payloadPath) != WellKnownFileTypeSuffix.PAYLOAD ){
            throw new IllegalArgumentException("Can not process " + payloadPath + ", unknown type");
        }

        Header header;

        // Parses the SBDH
        header = extractSbdhFromPayload(payloadPath);

        final SpiralisTask spiralisTask = new SpiralisTask(payloadPath, header);

        // Obtins our message identifier from the base of the payload filename
        final String ourMessageIdFromFileName = extractAndSetOurMessageIdFromFileName(payloadPath, spiralisTask);

        // See if we can find the S/MIME file
        // Constructs the Path of the AS2 MDN
        insertInformationFromMdnFile(payloadPath, spiralisTask, ourMessageIdFromFileName);

        // Extract and set the timestamp of reception
        obtainTimeStampFromPayloadCreationTimeStamp(payloadPath, spiralisTask);


        return spiralisTask;
    }


    private static void insertInformationFromMdnFile(Path payloadPath, SpiralisTask spiralisTask, String ourMessageIdFromFileName) {
        // Computes the name of the MDN (-rcpt.smime) file
        final Path smimePath = payloadPath.getParent().resolve(ourMessageIdFromFileName + WellKnownFileTypeSuffix.AS2_RECEIPT.getSuffix());

        // If it exists, obtain information and insert into our SpiralisTask object.
        if (Files.exists(smimePath)) {

            // Inserts the Path of the S/MIME file
            spiralisTask.setSmimePath(smimePath);

            // Extracts information from the S/MIME file
            final Grep grep = new Grep(smimePath.toFile());

            // Attempts to extract the AS2 Original-Message-ID, which represents the senders transmisssion ID
            final String transmissionId = grep.grepFirstGroup(messageIdPattern);
            spiralisTask.setTransmissionId(transmissionId);

            // Attempts to extract the sender of the original message, the receiver of the MDN (receipt)
            final String mdnReceiver = grep.grepFirstGroup(receiverOfMdnPattern);
            spiralisTask.setSendersApId(mdnReceiver);

        } else
            LOGGER.warn("S/MIME file {} not found", smimePath);
    }


    private static String extractAndSetOurMessageIdFromFileName(Path path, SpiralisTask spiralisTask) {
        final String baseFileName = SpiralisTaskFactory.baseFileName(path);

        // Base file name is expected to hold the UUID assigned by us upon reception.
        spiralisTask.setOurMessageId(baseFileName);
        return baseFileName;
    }

    private static void obtainTimeStampFromPayloadCreationTimeStamp(Path path, SpiralisTask spiralisTask) {
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
    }

    private static Header extractSbdhFromPayload(Path path) {
        Header header;
        try (final SbdReader sbdReader = SbdReader.newInstance(Files.newInputStream(path))) {

            // Extracts the SBDH from the input file
            header = sbdReader.getHeader();
        } catch (SbdhException e) {
            throw new IllegalStateException("Unable to retrieve SBDH from " + path + ", reason:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to process " + path + ", reason: " + e.getMessage(), e);
        }
        return header;
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
