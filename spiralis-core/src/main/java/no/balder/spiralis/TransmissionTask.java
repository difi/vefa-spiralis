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

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 16.55
 */
public class TransmissionTask implements Runnable {

    public static final Logger log = LoggerFactory.getLogger(TransmissionTask.class);

    private final OxalisOutboundComponent oxalisOutboundComponent;
    private final JmsConsumer jmsConsumer;
    private final AtomicInteger messageCounter;
    private final MessageConsumer messageConsumer;

    public TransmissionTask(OxalisOutboundComponent oxalisOutboundComponent, JmsConsumer jmsConsumer, AtomicInteger messageCounter) {

        this.oxalisOutboundComponent = oxalisOutboundComponent;
        this.jmsConsumer = jmsConsumer;
        messageConsumer = jmsConsumer.getMessageConsumer();

        this.messageCounter = messageCounter;
    }

    @Override
    public void run() {


        try {
            while (!Thread.currentThread().isInterrupted()) {
                processMessage();
                messageCounter.incrementAndGet();
            }
        } catch (JMSException e) {
            log.error("Unable to receive message " + e.getMessage(), e);
        }
    }

    void processMessage() throws JMSException {


        TransmissionRequestBuilder requestBuilder =
                oxalisOutboundComponent.getTransmissionRequestBuilder();

        Message message = messageConsumer.receive();
        OutboundTransmissionRequest req = MapMessageTransformer.valueOf(message);

        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(Paths.get(req.getPayloadUri()));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open file " + req.getPayloadUri());
        }

  /*      URL endPointUrl;
        try {
             endPointUrl = new URL("https://ap.hafslundtellier.no/oxalis/as2");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid url");
        }
*/

        TransmissionRequest transmissionRequest = requestBuilder
                .messageId(req.getMessageId())
                .documentType(PeppolDocumentTypeId.valueOf(req.getDocumentTypeId()))
                .processType(PeppolProcessTypeId.valueOf(req.getProcessTypeId()))
                .sender(new ParticipantId(req.getSender()))
                .receiver(new ParticipantId(req.getReceiver()))
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
