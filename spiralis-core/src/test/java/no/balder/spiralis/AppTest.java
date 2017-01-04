package no.balder.spiralis;

import com.google.inject.Inject;
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

    @Inject
    ObjectMother objectMother;
    private int messagesToProcess;

    @BeforeMethod(groups = TestModuleFactory.EXTERNAL_BROKER_GROUP)
    public void setUp() throws Exception {
        messagesToProcess = 10;
        objectMother.postSampleInvoices(messagesToProcess, Place.OUTBOUND_WORKFLOW_START);
    }

    @Test(groups = {TestModuleFactory.EXTERNAL_BROKER_GROUP})
    public void testMain() throws Exception {

        App.main(new String[] {"-e", "http://localhost:8080/oxalis/as2", "-b", "tcp://localhost:61616",
                "-m",""+messagesToProcess,   // Max 10 messages
                "-p", "2"
        });

    }

}