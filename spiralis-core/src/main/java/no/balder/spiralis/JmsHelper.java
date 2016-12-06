package no.balder.spiralis;

import com.google.inject.Inject;

import javax.jms.*;
import java.lang.IllegalStateException;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 15.32
 */
public class JmsHelper {

    QueueConnection queueConnection;

    @Inject
    public JmsHelper(QueueConnection queueConnection) {
        this.queueConnection = queueConnection;
    }


    JmsConsumer createTransactionalConsumerFor(String queueName) {

        try {
            QueueSession queueSession = queueConnection.createQueueSession(true, -1);
            Queue queue = queueSession.createQueue(queueName);
            MessageConsumer consumer = queueSession.createConsumer(queue);

            return new JmsConsumer(queueSession, consumer, true);

        } catch (JMSException e) {
            throw new IllegalStateException("Unable to create queue session for " + queueName);
        }
    }

    public MessageProducer createProducer(QueueSession queueSession, String queueName) {
        try {
            Queue queue = queueSession.createQueue(queueName);
            QueueSender sender = queueSession.createSender(queue);

            return sender;

        } catch (JMSException e) {
            throw new IllegalStateException("Unable to create queue " + e.getMessage(), e);
        }
    }
}
