package no.balder.spiralis;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.jms.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

/**
 * @author steinar
 *         Date: 15.12.2016
 *         Time: 14.15
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class WorkflowTest {

    public static final Logger log = LoggerFactory.getLogger(WorkflowTest.class);
    public static final int numberOfSamples = 10;

    @Inject
    ObjectMother objectMother;

    @Inject
    Connection connection;

    @Inject
    TaskFactory taskFactory;



    @AfterMethod
    public void cleanUp() throws JMSException {
        connection.stop();
    }

    @Test()
    public void testLoadSamples() throws Exception {

        List<OutboundTransmissionRequest> outboundTransmissionRequests = objectMother.postSampleInvoices(numberOfSamples, Place.OUTBOUND_WORKFLOW_START);

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        MessageConsumer consumer = session.createConsumer(session.createQueue(Place.OUTBOUND_WORKFLOW_START.getQueueName()));

        int count = 0;
        for (int i = 0; i < numberOfSamples; i++) {
            Message message = consumer.receive(1000);
            if (message == null) {
                fail("Reception of message timed out!");
            }
            count++;
        }
        assertEquals(count, numberOfSamples, "Should have received " + numberOfSamples + " messages.");
        log.info("Transmitted a total of " + numberOfSamples + " sample invoices");
    }


    @Test
    public void testLoadSamplesAndAddSbdh() throws Exception {

        // Loads sample invoices into the Outbound.reception queue
        List<OutboundTransmissionRequest> outboundTransmissionRequests = objectMother.postSampleInvoices(numberOfSamples, Place.OUTBOUND_WORKFLOW_START);
        assertEquals(outboundTransmissionRequests.size(), numberOfSamples);

        // Inspects the payload for Sbdh and posts the results into the Outbound.Validation queue
        SbdhInspectionTask sbdhInspectionTask = taskFactory.createSbdhInspectionTasks(1, Place.OUTBOUND_WORKFLOW_START,
                Place.OUTBOUND_VALIDATION,Place.OUTBOUND_INSPECTION_ERROR).get(0);
        ExecutorService sbhdInspectionExecutor = Executors.newFixedThreadPool(1);
        Future<?> future = sbhdInspectionExecutor.submit(sbdhInspectionTask);

        // Validates and places results into the Outbound.transmission queue
        List<ValidationTask> validatorTasks = taskFactory.createValidatorTasks(1,
                Place.OUTBOUND_VALIDATION, Place.OUTBOUND_TRANSMISSION, Place.OUTBOUND_INSPECTION_ERROR);
        ExecutorService validationExecutor = Executors.newFixedThreadPool(validatorTasks.size());
        for (ValidationTask validatorTask : validatorTasks) {
            Future<?> validationFuture = validationExecutor.submit(validatorTask);
        }

        // Performs the actual transmission.
        List<TransmissionTask> transmissionTasks = taskFactory.createTransmissionTasks(10,
                new URL("http://localhost:8080/oxalis/as2"),
                Place.OUTBOUND_TRANSMISSION, Place.OUTBOUND_TRANSMISSION_ERROR);

        ExecutorService transmissionExecutor = Executors.newFixedThreadPool(transmissionTasks.size());
        for (TransmissionTask transmissionTask : transmissionTasks) {
            transmissionExecutor.submit(transmissionTask);
        }

        connection.start();
        long processed = 0;
        int attempts =0;
        do {

            Thread.sleep(1000);
            log.debug("Waiting for Tasks to complete, " + attempts + " attempts so far...");
            processed = transmissionTasks.stream().collect(Collectors.summingLong(Task::getProcessCount));
            attempts++;

        } while (processed < numberOfSamples && attempts < (10));

        // Wait for clean up
        Thread.sleep(1000);

        List<Runnable> runnables = sbhdInspectionExecutor.shutdownNow();
        validationExecutor.shutdownNow();
        transmissionExecutor.shutdownNow();

        assertTrue(runnables.size() == 0, "Tasks (threads) did not shutdown properly");
        assertEquals(processed, numberOfSamples);
        log.info("Transmitted a total of " + numberOfSamples + " sample invoices");

    }
}
