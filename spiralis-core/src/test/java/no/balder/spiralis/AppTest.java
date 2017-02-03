package no.balder.spiralis;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 04.01.2017
 *         Time: 12.42
 */
@Guice( moduleFactory = TestModuleFactory.class)
@Test(groups = {TestModuleFactory.EXTERNAL_BROKER_GROUP})
public class AppTest {

    public static final Logger log = LoggerFactory.getLogger(AppTest.class);

    @Inject
    ObjectMother objectMother;
    private int messagesToProcess;

    @BeforeMethod(groups = TestModuleFactory.EXTERNAL_BROKER_GROUP)
    public void setUp() throws Exception {
        messagesToProcess = 50;
        objectMother.postSampleInvoices(messagesToProcess, Place.OUTBOUND_WORKFLOW_START);
        log.info("Posted " + messagesToProcess + " on the Outbound workflow start queue");
    }

    @Test(groups = {TestModuleFactory.EXTERNAL_BROKER_GROUP})
    public void testMain() throws Exception {

        App.main(new String[] {
                // "-e", "http://localhost:8080/oxalis/as2",
                "-e", "https://ap-test.hafslundtellier.no/oxalis/as2",
                // "-e", "https://test-aksesspunkt.difi.no/as2",
                "-b", "tcp://localhost:61616?jms.prefetchPolicy.queuePrefetch=2",
                "-m",""+messagesToProcess,
                "-p", "7"
        });

    }

}