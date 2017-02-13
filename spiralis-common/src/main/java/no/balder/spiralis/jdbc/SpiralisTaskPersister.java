package no.balder.spiralis.jdbc;

import no.balder.spiralis.inbound.SpiralisReceptionTask;
import no.balder.spiralis.transport.ReceptionId;
import no.balder.spiralis.transport.ReceptionMetaData;

import java.net.URI;
import java.util.Optional;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 18.21
 */
public interface SpiralisTaskPersister {

    Long saveInboundTask(SpiralisReceptionTask spiralisReceptionTask, URI payloadBlobUri, Optional<URI> smimeBlobUri);

    Optional<ReceptionMetaData> findByReceptionId(ReceptionId receptionId);
}
