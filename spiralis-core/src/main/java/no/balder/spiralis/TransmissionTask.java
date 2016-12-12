package no.balder.spiralis;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.outbound.OxalisOutboundComponent;
import eu.peppol.outbound.transmission.TransmissionRequest;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.TransmissionResponse;
import eu.peppol.outbound.transmission.Transmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.jms.IllegalStateException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 16.55
 */
public class TransmissionTask extends AbstractTask {

    public static final Logger log = LoggerFactory.getLogger(TransmissionTask.class);

    private final OxalisOutboundComponent oxalisOutboundComponent;
    private final ConsumerAdapter<OutboundTransmissionRequest> consumer;
    private final AtomicLong messageCounter = new AtomicLong(0);

    public TransmissionTask(OxalisOutboundComponent oxalisOutboundComponent, Session session, ConsumerAdapter<OutboundTransmissionRequest> consumer) {
        super(session);
        this.oxalisOutboundComponent = oxalisOutboundComponent;
        this.consumer = consumer;
    }

    @Override
    void processNextInputItem() throws JMSException {

        TransmissionRequestBuilder requestBuilder = oxalisOutboundComponent.getTransmissionRequestBuilder();

        OutboundTransmissionRequest request = consumer.receive();

        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(Paths.get(request.getPayloadUri()));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open file " + request.getPayloadUri());
        }

  /*      URL endPointUrl;
        try {
             endPointUrl = new URL("https://ap.hafslundtellier.no/oxalis/as2");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid url");
        }
*/

        TransmissionRequest transmissionRequest = requestBuilder
                .messageId(request.getMessageId())
                .documentType(PeppolDocumentTypeId.valueOf(request.getDocumentTypeId()))
                .processType(PeppolProcessTypeId.valueOf(request.getProcessTypeId()))
                .sender(new ParticipantId(request.getSender()))
                .receiver(new ParticipantId(request.getReceiver()))
                .payLoad(inputStream)
//                .overrideAs2Endpoint(endPointUrl, "AP_007")
                .build();

        Transmitter simpleTransmitter = oxalisOutboundComponent.getSimpleTransmitter();
        try {
            TransmissionResponse transmissionResponse = simpleTransmitter.transmit(transmissionRequest);
            // Now handle the responses

        } catch (OxalisTransmissionException e) {
            throw new IllegalStateException("Unable to transmit " + transmissionRequest);
        }


    }
}
