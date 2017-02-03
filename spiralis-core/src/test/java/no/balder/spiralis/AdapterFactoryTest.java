package no.balder.spiralis;

import com.google.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Singleton;
import javax.jms.Connection;
import javax.jms.Session;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 14.12.2016
 *         Time: 18.05
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class AdapterFactoryTest {

    @Inject @Singleton
    Connection connection;

    @Inject @Singleton
    Connection connection2;

    @Inject
    AdapterFactory consumerFactory;

    @Inject
    AdapterFactory producerFactory;

    /**
     * The JMS Connection should always be the same.
     *
     * @throws Exception when something goes wrong.
     */
    @Test
    public void testVerifyConnectionSingleton() throws Exception {
        assertEquals(connection, connection2);
    }

    @Test
    public void testCreateConsumer() throws Exception {

        Session writeSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Session readSession = connection.createSession(true, -1);

        ProducerAdapter<String> producer = producerFactory.createProducerAdapter(writeSession, new Place("TEST"));
        ConsumerAdapter<String> consumer = consumerFactory.createConsumerAdapter(readSession,new Place("TEST"));

        connection.start();
        producer.send("Hello world");

        String received = consumer.receive();
        assertEquals(received, "Hello world");
    }
}