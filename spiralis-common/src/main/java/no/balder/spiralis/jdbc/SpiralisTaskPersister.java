package no.balder.spiralis.jdbc;

import no.balder.spiralis.inbound.SpiralisTask;

import java.net.URI;
import java.util.Optional;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 18.21
 */
public interface SpiralisTaskPersister {

    Long saveInboundTask(SpiralisTask spiralisTask, URI payloadBlobUri, Optional<URI> smimeBlobUri);
}
