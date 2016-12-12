package no.balder.spiralis;

import javax.jms.*;
import java.lang.IllegalStateException;

/**
 * @author steinar
 *         Date: 12.12.2016
 *         Time: 16.51
 */
public class ConsumerAdapterImpl<T> implements ConsumerAdapter<T> {

    private final Session session;
    private final String queueName;
    private final MessageConsumer consumer;
    private Queue queue;

    public ConsumerAdapterImpl(Session session, String queueName) {
        this.session = session;
        this.queueName = queueName;

        try {
            queue = session.createQueue(queueName);
            consumer = session.createConsumer(queue);
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to create consumer for queue " + e.getMessage(), e);
        }
    }

    @Override
    public T receive() {

        try {
            Message msg = consumer.receive();
            if (msg instanceof ObjectMessage) {
                ObjectMessage m = (ObjectMessage) msg;
                return (T) m.getObject();
            } else
                throw new IllegalStateException("Unable to handle messages of type " + msg.getClass().getCanonicalName());
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to receive message " + e.getMessage(), e);
        }

    }
}
