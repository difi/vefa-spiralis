package no.balder.spiralis.inbound;

import com.sun.scenario.effect.Offset;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 10.57
 */
public class BlobName {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-MM-ss");;

    public static String createInboundBlobName(SpiralisTask spiralisTask, Supplier<Path> pathSupplier) {

        String isoDateTime = dateTimeFormatter.format(spiralisTask.getReceived());
        final String sender = spiralisTask.getHeader().getSender().getIdentifier().toString().replace(":","_");

        final String fileName = pathSupplier.get().getFileName().toString();
        final Path path = Paths.get("inbound",isoDateTime, sender, fileName);
        return path.toString();
    }

}


