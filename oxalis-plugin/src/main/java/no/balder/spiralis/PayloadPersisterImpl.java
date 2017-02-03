package no.balder.spiralis;

import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.vefa.peppol.common.model.Header;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author steinar
 *         Date: 24.01.2017
 *         Time: 08.35
 */
public class PayloadPersisterImpl implements PayloadPersister {

    public static String effectiveBrokerUrl = "tcp://localhost:61616?jms.prefetchPolicy.queuePrefetch=1";

    public PayloadPersisterImpl() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(effectiveBrokerUrl);
        try {
            Connection connection = activeMQConnectionFactory.createConnection();
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to connect to Apache MQ using " + effectiveBrokerUrl + " did you remember to start it?");
        }
    }

    @Override
    public Path persist(MessageId messageId, Header header, InputStream inputStream) throws IOException {

        System.err.println("Hello from payloadPersister ......");

        return Paths.get("/tmp");
    }
}
