package no.balder.spiralis;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.outbound.OxalisOutboundComponent;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.Transmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.IllegalStateException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 16.55
 */
public class TransmissionTask extends AbstractTask implements Task {

    public static final Logger log = LoggerFactory.getLogger(TransmissionTask.class);

    private final OxalisOutboundComponent oxalisOutboundComponent;
    private final ConsumerAdapter<OutboundTransmissionRequest> consumer;
    private final ProducerAdapter<OutboundTransmissionRequest> errorProducer;

    private final Optional<URL> overrideEndPointUrl;

    public TransmissionTask(OxalisOutboundComponent oxalisOutboundComponent, Session session, ConsumerAdapter<OutboundTransmissionRequest> consumer,
                            ProducerAdapter<OutboundTransmissionRequest> errorProducer) {
        this(oxalisOutboundComponent, session, consumer, errorProducer, null);
    }

    public TransmissionTask(OxalisOutboundComponent oxalisOutboundComponent, Session session, ConsumerAdapter<OutboundTransmissionRequest> consumer,
                            ProducerAdapter<OutboundTransmissionRequest> errorProducer, URL overrideEndPointUrl) {
        super(session);
        this.oxalisOutboundComponent = oxalisOutboundComponent;
        this.consumer = consumer;
        this.errorProducer = errorProducer;
        this.overrideEndPointUrl = Optional.ofNullable(overrideEndPointUrl);

    }

    @Override
    void processNextInputItem() throws JMSException, InterruptedException {

        TransmissionRequestBuilder requestBuilder = oxalisOutboundComponent.getTransmissionRequestBuilder();

        OutboundTransmissionRequest request = consumer.receive();

        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(Paths.get(request.getPayloadUri()));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open file " + request.getPayloadUri());
        }


        log.debug("MessageId to transmit is " + request.getMessageId());

        TransmissionRequestBuilder builder = requestBuilder
                .messageId(request.getMessageId())
                .documentType(PeppolDocumentTypeId.valueOf(request.getDocumentTypeId()))
                .processType(PeppolProcessTypeId.valueOf(request.getProcessTypeId()))
                .sender(new ParticipantId(request.getSender()))
                .receiver(new ParticipantId(request.getReceiver()))
                .payLoad(inputStream);

        if (overrideEndPointUrl.isPresent()) {
            try {
                builder.overrideAs2Endpoint(overrideEndPointUrl.get().toURI(), "AP_OVERRIDE");
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Unable to convert URL to URI" + e.getMessage(),e);
            }
        }

        TransmissionRequest transmissionRequest = null;
        try {
            transmissionRequest = builder.build();
        } catch (OxalisTransmissionException e) {
            throw new IllegalStateException("Unable to build request " + e.getMessage(), e);
        }


        Transmitter simpleTransmitter = oxalisOutboundComponent.getTransmitter();
        log.debug("Transmitting message using " + simpleTransmitter.getClass().getName());
        try {
            long start = System.nanoTime();
            TransmissionResponse transmissionResponse = simpleTransmitter.transmit(transmissionRequest);
            long end = System.nanoTime();
            long elapsed = TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS);

            log.info("Elapsed time for round-trip of " + request.getMessageId() + " took " + elapsed + "ms");
            // TODO: handle the responses


        } catch (Exception e) {

            // TODO: handle errors by writing them to the error queue (place)
            throw new IllegalStateException("Unable to transmit, error: " + e.getMessage() + transmissionRequest);
        }
    }
}
