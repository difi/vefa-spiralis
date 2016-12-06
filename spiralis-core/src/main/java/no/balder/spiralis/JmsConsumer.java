package no.balder.spiralis;

import javax.jms.MessageConsumer;
import javax.jms.QueueSession;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 15.16
 */
public class JmsConsumer {

    QueueSession queueSession;
    MessageConsumer messageConsumer;
    private final boolean transactional;

    public JmsConsumer(QueueSession queueSession, MessageConsumer messageConsumer, boolean transactional) {
        this.queueSession = queueSession;
        this.messageConsumer = messageConsumer;
        this.transactional = transactional;
    }

    public QueueSession getQueueSession() {
        return queueSession;
    }

    public MessageConsumer getMessageConsumer() {
        return messageConsumer;
    }

    public boolean isTransactional() {
        return transactional;
    }
}
