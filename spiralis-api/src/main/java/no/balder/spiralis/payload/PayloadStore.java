package no.balder.spiralis.payload;

import java.net.URI;
import java.nio.file.Path;
import java.time.OffsetDateTime;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 16.01
 */
public interface PayloadStore {
    URI upload(Path payload, String sender, String receiver, OffsetDateTime offsetDateTime);
}
