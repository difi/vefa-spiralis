package no.balder.spiralis;

import com.google.inject.Inject;

import javax.jms.*;
import java.lang.IllegalStateException;
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
    private final Connection jmsConnection;
    private final AdapterFactory adapterFactory;
    ExecutorService validatorExecutor;
    ExecutorService transmissionExecutor;

    AtomicInteger messageCounter = new AtomicInteger(0);
    private List<Future<?>> validationTasks;

    @Inject
    public OutboundWorkflow(Connection connection, AdapterFactory adapterFactory) {
        this.jmsConnection = connection;
        this.adapterFactory = adapterFactory;
    }

    public void start() {

        transmissionExecutor = Executors.newFixedThreadPool(TRANSMITTER_COUNT);

        validationTasks = createAndStartValidationTasks();

        try {
            jmsConnection.start();

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
/*
            JmsConsumer transactionalConsumerFor = jmsHelper.createTransactionalConsumerFor(Place.OUTBOUND_RECEPTION);
            MessageProducer producer = jmsHelper.createProducer(transactionalConsumerFor.getQueueSession(), Place.OUTBOUND_TRANSMISSION);
*/


/*
            ValidatorTask validatorTask = new ValidatorTask(validator, transactionalConsumerFor, producer, messageCounter);
            Future<?> submitted = validatorExecutor.submit(validatorTask);
            futures.add(submitted);
*/
        }

        return futures;
    }

    void createSbdhInspectionTask() throws JMSException {
        {
        }
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
