package no.balder.spiralis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author steinar
 *         Date: 07.12.2016
 *         Time: 18.27
 */
public abstract class AbstractTask implements Task {


    public static final Logger log = LoggerFactory.getLogger(AbstractTask.class);
    private final Session session;
    protected final AtomicLong processCount = new AtomicLong(0);


    public AbstractTask(Session session) {
        this.session = session;
    }

    @Override
    public void run() {

        try {
            while (!Thread.currentThread().isInterrupted()) {
                processNextInputItem();
                session.commit();
            }
        } catch (InterruptedException e) {
            log.info("Shutting down task due to interruption...");
        } catch (Exception e) {

            log.error("Error during processing of message: " + e.getMessage(), e);
            try {
                session.rollback();
            } catch (JMSException e1) {
                log.error("Unable to rollback JMS session: " + e.getMessage(), e);
                throw new IllegalStateException(e1);
            }
        } finally {
        }

    }

    void commit() throws JMSException {
        session.commit();
    }

    void rollback() throws JMSException {
        session.rollback();
    }

    public long getProcessCount() {
        return processCount.get();
    }

    abstract void processNextInputItem() throws JMSException, InterruptedException;


}
