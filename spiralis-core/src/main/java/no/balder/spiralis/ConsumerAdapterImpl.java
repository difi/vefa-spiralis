package no.balder.spiralis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.Serializable;
import java.lang.IllegalStateException;

/**
 * @author steinar
 *         Date: 12.12.2016
 *         Time: 16.51
 */
public class ConsumerAdapterImpl<T  extends Serializable> implements ConsumerAdapter<T> {

    public static final Logger log = LoggerFactory.getLogger(ConsumerAdapterImpl.class);
    private final Session session;
    private final Place place;
    private final MessageConsumer consumer;
    private Queue queue;

    public ConsumerAdapterImpl(Session session, Place place) {
        this.session = session;
        this.place = place;

        try {
            queue = session.createQueue(place.getQueueName());
            consumer = session.createConsumer(queue);
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to create consumer for queue " + e.getMessage(), e);
        }
    }

    @Override
    public T receive() throws InterruptedException {

        try {
            Message msg = consumer.receive();
            if (msg instanceof ObjectMessage) {
                ObjectMessage m = (ObjectMessage) msg;
                return (T) m.getObject();
            } else
                throw new IllegalStateException("Unable to handle messages of type " + msg.getClass().getCanonicalName());
        } catch (JMSException e) {
            if (e.getCause() != null && e.getCause() instanceof InterruptedException) {
                log.info("Shutting down...");
                throw new InterruptedException("Shutting down...");
            } else
                throw new IllegalStateException("Unable to receive message; " + e.getMessage(), e);
        }
    }



    @Override
    public Session getSession() {
        return session;
    }
}
