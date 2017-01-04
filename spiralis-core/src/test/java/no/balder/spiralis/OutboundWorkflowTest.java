package no.balder.spiralis;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.jms.Connection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 15.08
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class OutboundWorkflowTest {


    public static final Logger log = LoggerFactory.getLogger(OutboundWorkflowTest.class);


    @Inject
    OutboundWorkflowBuilder outboundWorkflowBuilder;

    @Inject
    ObjectMother objectMother;

    @Inject
    Connection connection;

    @Inject
    TaskFactory taskFactory;

    @Inject
    TransmissionTaskBuilder transmissionTaskBuilder;

    private int numberOfSamples = 10;

    @BeforeTest
    public void setUp() {
        objectMother.postSampleInvoices(numberOfSamples, Place.OUTBOUND_WORKFLOW_START);
    }


    @Test
    public void testWorkflow() throws InterruptedException {

        URL overrideEndPointUrl = null;
        try {
            overrideEndPointUrl = new URL("http://localhost:8080/oxalis/as2");
        } catch (MalformedURLException e) {
            throw new java.lang.IllegalStateException("Invalid URL: " + e.getMessage(), e);
        }


        OutboundWorkflow outboundWorkflow = new OutboundWorkflow(connection);

        List<TransmissionTask> transmissionTasks = taskFactory.createTransmissionTasks(1,
                overrideEndPointUrl,
                Place.OUTBOUND_WORKFLOW_START,
                Place.OUTBOUND_TRANSMISSION_ERROR);

        Transaction transmissionTx = outboundWorkflow.addTransmissionTransaction(transmissionTasks);

        outboundWorkflow.start();


        long processed = 0;
        int attempts =0;
        do {

            Thread.sleep(1000);
            log.debug("Waiting for Tasks to complete, " + attempts + " attempts so far...");
            processed = transmissionTx.getProcessCount();
            attempts++;

        } while (processed < numberOfSamples && attempts < (100));

        assertEquals(processed, numberOfSamples);

        outboundWorkflow.stop();
    }

    @Test
    public void testWithTransmissionTaskBuilder() throws Exception {

        OutboundWorkflow outboundWorkflow = new OutboundWorkflow(connection);

        assertEquals(connection, transmissionTaskBuilder.getTaskFactory().getJmsConnection());

        List<TransmissionTask> transmissionTasks = transmissionTaskBuilder.errorPlace(Place.OUTBOUND_TRANSMISSION_ERROR)
                .inputPlace(Place.OUTBOUND_WORKFLOW_START)
                .overrideEndpointUrl("http://localhost:8080/oxalis/as2")
                .build(1);

        Transaction transmissionTx = outboundWorkflow.addTransmissionTransaction(transmissionTasks);

        outboundWorkflow.start();

        long processed = 0;
        int attempts =0;
        do {

            Thread.sleep(1000);
            log.debug("Waiting for Tasks to complete, " + attempts + " attempts so far...");
            processed = transmissionTx.getProcessCount();
            attempts++;

        } while (processed < numberOfSamples && attempts < (100));

        assertEquals(processed, numberOfSamples);

        outboundWorkflow.stop();
    }


    @Test
    public void testUsingOutboundWorkflowBuilder() throws Exception {

        OutboundWorkflow outboundWorkflow = outboundWorkflowBuilder
                .transmissionEndPoint("http://localhost:8080/oxalis/as2")
                .trasmissionTaskCount(1)
                .build();

        outboundWorkflow.start();

        long processed = 0;
        int attempts =0;
        do {

            Thread.sleep(1000);
            log.debug("Waiting for Tasks to complete, " + attempts + " attempts so far...");
            processed = outboundWorkflow.getTransmissionTransaction().getProcessCount();
            attempts++;

        } while (processed < numberOfSamples && attempts < (100));

        assertEquals(processed, numberOfSamples);

        outboundWorkflow.stop();
    }
}