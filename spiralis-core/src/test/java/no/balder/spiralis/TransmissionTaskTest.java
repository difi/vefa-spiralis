package no.balder.spiralis;

import com.google.inject.Inject;
import eu.peppol.outbound.OxalisOutboundComponent;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import static no.balder.spiralis.Place.*;
import javax.jms.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    Connection queueConnection;

    @Inject
    ConnectionFactory queueConnectionFactory;

    @BeforeTest
    public void setUp() {
        objectMother.postIdenticalSampleInvoicesToOutboundTransmissionQueue(10);
    }

    @Test
    public void testTransmission() throws JMSException, InterruptedException {


        Session session = queueConnection.createSession(true, -1);

        ConsumerAdapterImpl<OutboundTransmissionRequest> consumer = new ConsumerAdapterImpl<>(session, OUTBOUND_TRANSMISSION);
        TransmissionTask transmissionTask = new TransmissionTask(new OxalisOutboundComponent(), session, consumer);
        queueConnection.start();

        // Processes a single message.
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> submit = executorService.submit(transmissionTask);

        Thread.sleep(100*1000);


    }
}