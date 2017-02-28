package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.ReceptionPathUtil;
import no.balder.spiralis.transport.ReceptionId;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Supplier;

/**
 * Computes the Blob name for a given file.
 *
 * <pre>
 *     inbound/yy-mm-ddThh-mm-ss/sender/filename.type
 * </pre>
 *
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 10.57
 */
public class BlobName {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-MM-ss");;

    public static String createInboundBlobName(ReceptionId receptionId, Date timeStamp, ParticipantIdentifier sender, Path path) {

        String isoDateTime = dateTimeFormatter.format(LocalDateTime.ofInstant(timeStamp.toInstant(), ZoneId.systemDefault()));
        final String senderValue = sender.getIdentifier().toString().replace(":","_");

        final String fileNameBodyPart = ReceptionPathUtil.fileNameBodyPart(path);

        final String fileName = path.getFileName().toString();
        final String newName = fileName.replace(fileNameBodyPart, receptionId.toString());


        // Creates the path as inbound/yyyy-MM-ddTHH-MM-SS/icd:orgno/receptionId-xxx.yyy
        final Path pathResult = Paths.get("inbound",isoDateTime, senderValue, newName);

        return pathResult.toString();
    }

}


