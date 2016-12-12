package no.balder.spiralis;

import com.google.inject.Inject;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.testng.annotations.*;

import javax.jms.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 12.12.2016
 *         Time: 09.13
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class AdapterTest {

    private MessageProducer producer;
    private Session session;
    private MessageConsumer consumer;
    private Session readSession;
    private Connection connection;

    @Inject
    ActiveMQConnectionFactory activeMQConnectionFactory;

    @BeforeClass
    public void setUp() throws JMSException {

/*
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        activeMQConnectionFactory.setTrustAllPackages(true);
*/

        connection = activeMQConnectionFactory.createConnection();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("UNIT.TEST");
        producer = session.createProducer(queue);

        readSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue readQ = readSession.createQueue("UNIT.TEST");
        consumer = readSession.createConsumer(readQ);


    }

    @AfterMethod
    public void tearDown() throws JMSException {
        connection.stop();
    }

    /**
     * Verifies that we can send any {@link java.io.Serializable} object through the queue.
     * @throws JMSException
     */
    @Test
    public void testBasicFunctionality() throws JMSException {

        ObjectMessage objectMessage = session.createObjectMessage("Hello World");

        producer.send(objectMessage);

        connection.start();

        Message receive = consumer.receive();
        assertTrue(receive instanceof ObjectMessage);

        ObjectMessage o2 = (ObjectMessage) receive;
        String s = (String) o2.getObject();

        assertEquals(s, "Hello World");
    }

    /**
     * Uses the adapters for writing and reading from the test queue
     * @throws JMSException
     */
    @Test
    public void testWriteReadWithAdapter() throws JMSException {

        String queueName = "UNIT.TEST2";

        ProducerAdapterImpl<OutboundTransmissionRequest> producer = new ProducerAdapterImpl<>(session, queueName);

        OutboundTransmissionRequest sendt = ObjectMother.sampleOutboundTransmissionRequest();
        producer.send(sendt);

        connection.start();

        ConsumerAdapterImpl<OutboundTransmissionRequest> consumer = new ConsumerAdapterImpl<>(session, queueName);
        OutboundTransmissionRequest received = consumer.receive();

        assertEquals(received, sendt);
    }
}
