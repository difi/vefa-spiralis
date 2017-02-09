package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.PayloadPathUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
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

    public static String createInboundBlobName(SpiralisReceptionTask spiralisReceptionTask, Supplier<Path> pathSupplier) {

        String isoDateTime = dateTimeFormatter.format(spiralisReceptionTask.getReceived());
        final String sender = spiralisReceptionTask.getHeader().getSender().getIdentifier().toString().replace(":","_");

        final Path pathOfFileToUpload = pathSupplier.get();

        final String fileNameBodyPart = PayloadPathUtil.fileNameBodyPart(pathOfFileToUpload);

        final String fileName = pathOfFileToUpload.getFileName().toString();
        final String newName = fileName.replace(fileNameBodyPart, spiralisReceptionTask.getReceptionId().toString());


        // Creates the path as inbound/yyyy-MM-ddTHH-MM-SS/icd:orgno/receptionId-xxx.yyy
        final Path path = Paths.get("inbound",isoDateTime, sender, newName);

        return path.toString();
    }

}


