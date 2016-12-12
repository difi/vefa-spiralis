package no.balder.spiralis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 *
 *
 * @author steinar
 *         Date: 07.12.2016
 *         Time: 18.27
 */
public abstract class AbstractTask implements Task {


    public static final Logger log = LoggerFactory.getLogger(AbstractTask.class);
    private final Session queueSession;

    public AbstractTask(Session queueSession) {
        this.queueSession = queueSession;
    }

    @Override
    public void run() {

        try {
            while (!Thread.currentThread().isInterrupted()) {
                processNextInputItem();
            }
        } catch (Exception e) {
            log.error("Error during processing of message. " + e.getMessage(), e);
            try {
                queueSession.rollback();
            } catch (JMSException e1) {
                log.error("Unable to rollback JMS session: " + e.getMessage(), e);
                throw new IllegalStateException(e1);
            }
        } finally {
        }

    }

    void commit() throws JMSException {
        queueSession.commit();
    }

    void rollback() throws JMSException {
        queueSession.rollback();
    }

    abstract void processNextInputItem() throws JMSException;


}
