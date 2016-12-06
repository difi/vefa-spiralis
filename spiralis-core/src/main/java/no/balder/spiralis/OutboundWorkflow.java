package no.balder.spiralis;

import com.google.inject.Inject;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 14.41
 */
public class OutboundWorkflow {


    public static final int VALIDATOR_COUNT = 3;
    public static final int TRANSMITTER_COUNT = 20;
    private final QueueConnection queueConnection;
    private final JmsHelper jmsHelper;
    ExecutorService validatorExecutor;
    ExecutorService transmissionExecutor;

    AtomicInteger messageCounter = new AtomicInteger(0);
    private List<Future<?>> validationTasks;

    @Inject
    public OutboundWorkflow(QueueConnection queueConnection, JmsHelper jmsHelper) {
        this.queueConnection = queueConnection;
        this.jmsHelper = jmsHelper;
    }

    public void start() {

        transmissionExecutor = Executors.newFixedThreadPool(TRANSMITTER_COUNT);

        validationTasks = createAndStartValidationTasks();

        try {
            queueConnection.start();
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to start JMS queue connection " + e, e);
        }
    }


    /**
     * Creates and submits a set of validation tasks.
     *
     * @return
     */
    List<Future<?>> createAndStartValidationTasks() {
        validatorExecutor = Executors.newFixedThreadPool(VALIDATOR_COUNT);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < VALIDATOR_COUNT; i++) {
            JmsConsumer transactionalConsumerFor = jmsHelper.createTransactionalConsumerFor(QueueConstant.OUTBOUND_RECEPTION);
            MessageProducer producer = jmsHelper.createProducer(transactionalConsumerFor.getQueueSession(), QueueConstant.OUTBOUND_TRANSMISSION);


/*
            ValidatorTask validatorTask = new ValidatorTask(validator, transactionalConsumerFor, producer, messageCounter);
            Future<?> submitted = validatorExecutor.submit(validatorTask);
            futures.add(submitted);
*/
        }

        return futures;
    }

    public AtomicInteger getMessageCounter() {
        return messageCounter;
    }

    public void stop() {
        // Prevents new tasks from
        validatorExecutor.shutdown();
        transmissionExecutor.shutdown();

        for (Future<?> validationTask : validationTasks) {
            validationTask.cancel(true);
        }

        validatorExecutor.shutdownNow();
    }
}
