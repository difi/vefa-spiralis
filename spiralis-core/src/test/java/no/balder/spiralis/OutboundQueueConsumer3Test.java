package no.balder.spiralis;

import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.persistence.AccountId;
import eu.peppol.persistence.ChannelProtocol;
import eu.peppol.persistence.MessageMetaData;
import eu.peppol.persistence.TransferDirection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.jms.*;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 29.11.2016
 *         Time: 10.17
 */
public class OutboundQueueConsumer3Test implements ExceptionListener {

    public static final Logger log = LoggerFactory.getLogger(OutboundQueueConsumer3Test.class);

    public static final int MSG_COUNT = 100;
    public static final boolean JMS_TRANSACTIONAL = true;
    URL payloadUrl = OutboundQueueConsumer3Test.class.getClassLoader().getResource("hafslund-test-1.xml");
    private QueueSession producerSession;
    private QueueConnection queueConnection;
    private ExecutorService executorService;

    @Test
    public void testOutboundQeueueConsumer() {


        assertNotNull(payloadUrl, "Unable to locate test resource " + "hafslund-test-1.xml");

        // Holds the messges received by the consumer in order to verify that all messages were received
        ConcurrentLinkedQueue<Object> receivedMessages = new ConcurrentLinkedQueue<>();

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        executorService = Executors.newFixedThreadPool(3);

        List<Future> futures = new ArrayList<>();
        Queue testQeueue = null;
        try {
            // Connects to the JMS broker
            queueConnection = activeMQConnectionFactory.createQueueConnection();

            // Creates processes for consumption of messages
            for (int i = 0; i < 3; i++) {
                QueueSession queueSession = queueConnection.createQueueSession(JMS_TRANSACTIONAL, -1);
                ProcessorTask processorTask = new ProcessorTask(queueSession, receivedMessages);
                Future<?> future = executorService.submit(processorTask);
                futures.add(future);
            }

            // Do not use transactional mode for producing messages.
            producerSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            testQeueue = producerSession.createQueue("TEST");
            MessageProducer producer = producerSession.createProducer(testQeueue);
            // Starts the connection allowing messages to flow
            queueConnection.start();

            queueConnection.setExceptionListener(this);

            for (int i = 0; i < MSG_COUNT; i++) {
                MessageMetaData messageMetaData = createMessageMetaData();
                MapMessage m = MapMessageTransformer.from(producerSession.createMapMessage(), ObjectMother.sampleOutboundTransmissionRequest());
                producer.send(m);
            }


        } catch (JMSException e) {
            e.printStackTrace();
        }

        final Thread mainThread = Thread.currentThread();
        final Queue writingQeuue = testQeueue;
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {

                System.out.println("Shutting down...");
                executorShutdown(executorService);
                System.out.println("Executor service has been shut down");

                int size = receivedMessages.size();
                System.out.println("Received " + size + " messages");

                try {
                    QueueBrowser browser = producerSession.createBrowser(writingQeuue);
                    Enumeration enumeration = browser.getEnumeration();
                    int count = 0;
                    while (enumeration.hasMoreElements()) {
                        enumeration.nextElement();
                        count++;
                    }

                    assertEquals(count, 0, "There are still messages left in the queue");

                } catch (JMSException e) {
                    fail(e.getMessage());
                }


                try {
                    producerSession.close();
                } catch (JMSException e) {
                    System.err.println("Unable to close the JMS session " + e.getMessage());
                }
                try {
                    queueConnection.close();
                } catch (JMSException e) {
                    System.err.println("Unable to close the connection " + e.getMessage());
                }
                mainThread.interrupt();
            }
        });

        // Cancels the first thread.
        futures.get(0).cancel(true);

        try {
            log.debug("Sleeping for 2seconds .....");
            Thread.sleep( 2*1000L);
        } catch (InterruptedException e) {
            System.err.println("Interrupted while sleeping...");
        }

        log.debug(receivedMessages.size() + " received so far, shutting down now .....");


        assertEquals(receivedMessages.size(), MSG_COUNT);
        log.debug("Now, let's call the stop hook....");
    }

    private void executorShutdown(ExecutorService executorService) {
        executorService.shutdown();   // Prevents new tasks from being submitted
        try {
            if (!executorService.awaitTermination(2L, TimeUnit.SECONDS)) {
                // Wait for the existing tasks to terminate...
                List<Runnable> runnables = executorService.shutdownNow();

            } else
                log.info("ExecutorService has been stop");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    MessageMetaData createMessageMetaData() {

        MessageMetaData.Builder builder = new MessageMetaData.Builder(TransferDirection.OUT,
                WellKnownParticipant.DUMMY, WellKnownParticipant.DIFI_TEST,
                PeppolDocumentTypeIdAcronym.EHF_INVOICE.getDocumentTypeIdentifier(), ChannelProtocol.AS2);

        try {
            builder.accountId(new AccountId(1))
                    .messageId(new MessageId())
                    .payloadUri(payloadUrl.toURI());
            return builder.build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to convert URL " + payloadUrl + " to URI: " + e.getMessage());
        }
    }

    @Override
    public void onException(JMSException e) {
        log.error("JMS Exception received: " + e.getMessage(), e);
    }

    // Refactor into separate class once experiments are finished.
    static class ProcessorTask implements Runnable {

        public static final Logger log = LoggerFactory.getLogger(ProcessorTask.class);
        private final QueueSession queueSession;
        private final ConcurrentLinkedQueue<Object> receivedMessages;
        private AtomicInteger atomicInteger = new AtomicInteger(1);

        public ProcessorTask(QueueSession queueSession, ConcurrentLinkedQueue<Object> receivedMessages) {
            this.queueSession = queueSession;

            this.receivedMessages = receivedMessages;

        }

        @Override
        public void run() {
            MessageConsumer consumer = null;
            try {
                Queue testQeueue = queueSession.createQueue("TEST");
                consumer = queueSession.createConsumer(testQeueue);

            } catch (JMSException e) {
                log.error("Unable to create queue session:" + e.getMessage(), e);
            }

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Message message = consumer.receive();
                    if (message instanceof MapMessage) {
                        MapMessage m = (MapMessage) message;

                        OutboundTransmissionRequest outboundTransmissionRequest = MapMessageTransformer.valueOf(m);
                        log.debug(atomicInteger.addAndGet(1) + " " + outboundTransmissionRequest.toString());

                        Path path = Paths.get(outboundTransmissionRequest.getPayloadUri());

                        receivedMessages.add(outboundTransmissionRequest);
                        queueSession.commit();
                    } else if (message != null) {
                        log.error("Received message of unknown type: " + message.getClass().getName());
                    }
                }
                log.info("Thread interrupted, cancelling operations.");

            } catch (JMSException e) {
                if (e.getCause() != null && (e.getCause() instanceof InterruptedException || e.getCause() instanceof java.io.InterruptedIOException)) {
                    log.error("JMS interrupted, shutting down this consumer ");
                    log.error("JMS interrupted, shutting down, cause: " + e.getCause());
                } else
                    log.error("Unable to receive message from queue: " + e.getMessage(), e);
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    log.info("Interrupted, shutting down....");
                } else
                    log.error("Caught exception " + e.getMessage(), e);
            }
        }
    }
}