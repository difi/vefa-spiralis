package no.balder.spiralis;

import javax.jms.*;
import java.io.Serializable;
import java.lang.IllegalStateException;

/**
 * @author steinar
 *         Date: 12.12.2016
 *         Time: 15.32
 */
public class ProducerAdapterImpl<T extends Serializable> implements ProducerAdapter<T> {

    private final Session session;
    private final Queue queue;
    private final MessageProducer producer;
    private String queueName = "unknown";

    public ProducerAdapterImpl(final Session session, final String queueName) {

        this.session = session;
        this.queueName = queueName;


        try {
            queue = session.createQueue(queueName);
            producer = session.createProducer(queue);
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to create producer for queue " + queueName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void send(T msg) {

        try {
            ObjectMessage objectMessage = session.createObjectMessage(msg);
            producer.send(objectMessage);
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to send message: " + e.getMessage(), e);
        }
    }
}
