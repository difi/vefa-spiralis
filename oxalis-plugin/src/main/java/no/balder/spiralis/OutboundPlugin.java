package no.balder.spiralis;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.lang.IllegalStateException;

/**
 *
 */
public class OutboundPlugin {

    public static final Logger log = LoggerFactory.getLogger(OutboundPlugin.class);
    private final ActiveMQConnectionFactory activeMQConnectionFactory;
    private Connection connection;

    private  boolean connectionOk;

    private OutboundPlugin() {
        activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        activeMQConnectionFactory.setTrustAllPackages(true);
        log.info("Createing OutboundPlugin with connection to ActiveMQ");
        try {
            connection = activeMQConnectionFactory.createConnection();
            connectionOk = true;
        } catch (JMSException e) {
            String msg = "Unable to create connection to ActiveMQ; reason: " + e.getMessage();
            log.error(msg, e);
            connectionOk = false;
        }
    }

    public static OutboundPlugin getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void makeOutboundRequest(OutboundTransmissionRequest request) {
        if (connectionOk == false) {
            log.warn("Unable to post outbound message request, no connection");
            return;
        }

        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(Place.OUTBOUND_WORKFLOW_START.getQueueName());
            MessageProducer producer = session.createProducer(queue);
            ObjectMessage objectMessage = session.createObjectMessage(request);

            producer.send(objectMessage);

            log.debug("Message sent");
        } catch (JMSException e) {
            String msg = "Unable to create connection to ActiveMQ; reason: " + e.getMessage();
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    private static class LazyHolder {
        private static final OutboundPlugin INSTANCE = new OutboundPlugin();
    }
}
