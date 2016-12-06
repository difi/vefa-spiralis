package no.balder.spiralis;

import com.google.inject.Inject;
import eu.peppol.outbound.OxalisOutboundComponent;
import eu.peppol.outbound.transmission.Transmitter;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.jms.JMSException;
import javax.jms.QueueConnection;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 16.59
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class TransmissionTaskTest {

    @Inject
    ObjectMother objectMother;

    @javax.inject.Inject
    QueueConnection queueConnection;

    @Inject
    JmsHelper jmsHelper;

    @BeforeTest
    public void setUp() {
        objectMother.postSampleInvoiceToOutboundTransmissionQueue(10);
    }

    @Test
    public void testTransmission() throws JMSException {

        JmsConsumer consumerFor = jmsHelper.createTransactionalConsumerFor(QueueConstant.OUTBOUND_TRANSMISSION);
        AtomicInteger messageCounter = new AtomicInteger();

        TransmissionTask transmissionTask = new TransmissionTask(new OxalisOutboundComponent(), consumerFor, messageCounter);
        queueConnection.start();

        // Processes a single message.
        transmissionTask.processMessage();

    }
}