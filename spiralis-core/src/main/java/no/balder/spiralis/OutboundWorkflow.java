package no.balder.spiralis;

import com.google.inject.Inject;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.util.List;

/**
 *
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 14.41
 */
public class OutboundWorkflow {


    private final Connection jmsConnection;

    private Transaction transmissionTransaction;

    private boolean started = false;

    @Inject
    public OutboundWorkflow(Connection connection) {
        this.jmsConnection = connection;
    }


    public void start() {
        if (started) {
            throw new IllegalStateException("OutboundWorkflow must not be started twice");
        }
        transmissionTransaction.start();

        try {
            jmsConnection.start();

        } catch (JMSException e) {
            throw new IllegalStateException("Unable to start JMS queue connection " + e, e);
        }
        started = true;
    }


    public void stop() {

        transmissionTransaction.getExecutorService().shutdown();

        // TODO: Consider cancelling each task in each transaction

        transmissionTransaction.getExecutorService().shutdownNow();
        started = false;
    }


    public Transaction addTransmissionTransaction(List<TransmissionTask> tasks) {

        transmissionTransaction = new Transaction(tasks);

        return transmissionTransaction;
    }

    public Transaction getTransmissionTransaction() {
        return transmissionTransaction;
    }
}
