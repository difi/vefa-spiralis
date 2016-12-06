package no.balder.spiralis;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.NoSbdhParser;
import eu.peppol.identifier.MessageId;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.jms.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.IllegalStateException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 10.41
 */
public class ObjectMother {


    public static final String SAMPLE_INVOICE_RESOURCE_NAME = "hafslund-test-1.xml";
    private final QueueConnection queueConnection;

    @Inject
    public ObjectMother(QueueConnection queueConnection) {
        this.queueConnection = queueConnection;
    }

    public static URL sampleInvoice() {
        URL resource = ObjectMother.class.getClassLoader().getResource(SAMPLE_INVOICE_RESOURCE_NAME);

        assertNotNull(resource, "Unable to locate " + SAMPLE_INVOICE_RESOURCE_NAME + " in classpath");
        return resource;
    }

    public static URI sampleInvoiceURI() {
        try {
            return sampleInvoice().toURI();

        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to convert URL to UIR " + e.getMessage(), e);
        }
    }

    public static InputStream sampleInvoiceStream() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(sampleInvoice().toURI()));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("File not found " + e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URI " + e.getMessage(), e);
        }

        assertNotNull(fileInputStream);
        return fileInputStream;
    }


    public static  PeppolStandardBusinessHeader parseSample() {
        URL url = sampleInvoice();

        NoSbdhParser noSbdhParser = new NoSbdhParser();
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = noSbdhParser.parse(sampleInvoiceStream());
        return peppolStandardBusinessHeader;
    }


    public void postSampleInvoiceToOutboundReceptionQueue(int count) {


        OutboundTransmissionRequest outboundTransmissionRequest = sampleOutboundTransmissionRequest();

        try {
            QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QueueConstant.OUTBOUND_RECEPTION);
            QueueSender sender = queueSession.createSender(queue);
            MapMessage mapMessage = queueSession.createMapMessage();


            try {
                for (int i = 0; i < count; i++) {


                    MapMessage message = MapMessageTransformer.from(mapMessage, outboundTransmissionRequest);
                    sender.send(message);
                }
            } finally {
                queueSession.close();
            }


        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static OutboundTransmissionRequest sampleOutboundTransmissionRequest() {
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = ObjectMother.parseSample();

        return new OutboundTransmissionRequest(new MessageId(),
                sampleInvoiceURI(),
                true,
                peppolStandardBusinessHeader.getSenderId().stringValue(),
                peppolStandardBusinessHeader.getRecipientId().stringValue(),
                peppolStandardBusinessHeader.getDocumentTypeIdentifier().toString(),
                peppolStandardBusinessHeader.getProfileTypeIdentifier().toString());
    }

    public void postSampleInvoiceToOutboundTransmissionQueue(int count) {
        OutboundTransmissionRequest outboundTransmissionRequest = sampleOutboundTransmissionRequest();

        try {
            QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QueueConstant.OUTBOUND_TRANSMISSION);
            MessageProducer producer = queueSession.createProducer(queue);

            try {
                for (int i = 0; i < count; i++) {
                    MapMessage mapMessage = MapMessageTransformer.from(queueSession.createMapMessage(), outboundTransmissionRequest);
                    producer.send(mapMessage);
                }
            } finally {
                queueSession.close();
            }


        } catch (JMSException e) {
            throw new IllegalStateException("Unable to perform JMS operation: " + e.getMessage(), e);
        }
    }
}
