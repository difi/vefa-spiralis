package no.balder.spiralis;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.jms.*;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 07.12.2016
 *         Time: 19.18
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class SbdhInspectionTaskTest {

    public static final Logger log = LoggerFactory.getLogger(SbdhInspectionTaskTest.class);
    @Inject
    Connection queueConnection;


    private Session queueSession;
    private ConsumerAdapter<OutboundTransmissionRequest> consumerAdapter;
    private ProducerAdapter<OutboundTransmissionRequest> producerAdapter;
    private ProducerAdapter<String> errorProducerAdapter;

    @BeforeTest
    public void setUp() throws JMSException {

        queueSession = queueConnection.createSession(true, -1);

        consumerAdapter = new ConsumerAdapterImpl<OutboundTransmissionRequest>(queueSession, "UNIT.TEST");
        producerAdapter = new ProducerAdapterImpl<OutboundTransmissionRequest>(queueSession, "UNIT.TEST.WRITE");
        errorProducerAdapter = new ProducerAdapterImpl<String>(queueSession, "UNIT.TEST.ERROR");


        // Submits a sample outbound transmission request into the system.
        Session testSession = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = testSession.createQueue("UNIT.TEST");
        MessageProducer testDataProducer = testSession.createProducer(queue);
        ObjectMessage objectMessage = testSession.createObjectMessage(ObjectMother.sampleOutboundTransmissionRequest());
        testDataProducer.send(objectMessage);
    }

    /**
     * Send a single {@link OutboundTransmissionRequest} object through the queue system and verify that it has been processed
     * by the {@link SbdhInspectionTask}
     *
     * @throws Exception
     */
    @Test
    public void testProcessNextInputItem() throws Exception {

        queueConnection.start();
        SbdhInspectionTask sbdhInspectionTask = new SbdhInspectionTask(queueSession, consumerAdapter, producerAdapter, errorProducerAdapter);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<?> sbdhFutureTask = executorService.submit(sbdhInspectionTask);

        int count = 0;
        do {
            if (count++ > 3) {
                break;
            }
            Thread.sleep(10);
        } while (sbdhInspectionTask.getProcessCount() == 0L);

        boolean cancel = sbdhFutureTask.cancel(true);

        assertEquals(sbdhInspectionTask.getProcessCount(), 1L);

        // Verifies that the input message was actually writte to the output queue
        Queue destinationQueue = queueSession.createQueue("UNIT.TEST.WRITE");

        MessageConsumer consumer = queueSession.createConsumer(destinationQueue);
        Message m = consumer.receive();
        assertTrue(m instanceof ObjectMessage);

        ObjectMessage om = (ObjectMessage) m;
        OutboundTransmissionRequest otr = (OutboundTransmissionRequest) om.getObject();

    }

}
