package no.balder.spiralis;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 15.08
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class OutboundWorkflowTest {


    public static final Logger log = LoggerFactory.getLogger(OutboundWorkflowTest.class);
    @Inject
    OutboundWorkflow outboundWorkflow;

    @Inject
    ObjectMother objectMother;

    @BeforeTest
    public void setUp() {
        objectMother.postIdenticalSampleInvoicesToOutboundReceptionQueue(10);
    }


    @Test
    public void testWorkflow() throws InterruptedException {

        outboundWorkflow.start();

        while (outboundWorkflow.getMessageCounter().get() < 10) {
            log.debug("Waiting for outboundworkflow to process 10 messages. " + outboundWorkflow.getMessageCounter().get() + " processed so far");
            Thread.sleep(1*1000L);
        }

        outboundWorkflow.stop();
    }
}