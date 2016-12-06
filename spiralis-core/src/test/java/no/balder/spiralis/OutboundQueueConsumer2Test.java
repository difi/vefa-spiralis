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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 29.11.2016
 *         Time: 10.17
 */
public class OutboundQueueConsumer2Test {

    public static final Logger log = LoggerFactory.getLogger(OutboundQueueConsumer2Test.class);

    public static final int MSG_COUNT = 100;
    public static final boolean JMS_TRANSACTIONAL = true;
    URL payloadUrl = OutboundQueueConsumer2Test.class.getClassLoader().getResource("hafslund-test-1.xml");
    private QueueSession queueSession;
    private QueueConnection queueConnection;

    @Test
    public void testOutboundQeueueConsumer() {


        assertNotNull(payloadUrl, "Unable to locate test resource " + "hafslund-test-1.xml");

        // Holds the messges received by the consumer in order to verify that all messages were received
        ConcurrentLinkedQueue<Object> receivedMessages = new ConcurrentLinkedQueue<>();

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();

        List<Thread> threads = new ArrayList<>();
        Queue testQeueue = null;
        try {
            // Connects to the JMS broker
            queueConnection = activeMQConnectionFactory.createQueueConnection();

            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            testQeueue = queueSession.createQueue("TEST");
            MessageProducer producer = queueSession.createProducer(testQeueue);
            // Starts the connection allowing messages to flow
            queueConnection.start();

            for (int i = 0; i < MSG_COUNT; i++) {
                MessageMetaData messageMetaData = createMessageMetaData();
                MapMessage m = MapMessageTransformer.from(queueSession.createMapMessage(), ObjectMother.sampleOutboundTransmissionRequest());
                producer.send(m);
            }


            // Creates processes for consumption of messages
            for (int i = 0; i < 3; i++) {
                ProcessorTask processorTask = new ProcessorTask(queueConnection, receivedMessages);
                Thread thread = new Thread(processorTask, "SpiralisWorker-" + i);
                threads.add(thread);
                thread.start();
            }


        } catch (JMSException e) {
            e.printStackTrace();
        }

        final Thread mainThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                System.out.println("Shutting down...");
                System.out.println("Executor service has been shut down");
                int size = receivedMessages.size();
                System.out.println("Received " + size + " messages");
                try {
                    queueSession.close();
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

        try {
            Thread.sleep(2 * 1000L);
        } catch (InterruptedException e) {
            System.err.println("Interrupted while sleeping...");
        }

        for (Thread thread : threads) {
            thread.interrupt();
            try {
                thread.join(1000L);
            } catch (InterruptedException e) {
                log.error("Interrupting failed." + e.getMessage(), e);
            }
        }

        try {
            QueueBrowser browser = queueSession.createBrowser(testQeueue);
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
        assertEquals(receivedMessages.size(), MSG_COUNT, "All message have not been processed");
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

    // Refactor into separate class once experiments are finished.
    static class ProcessorTask implements Runnable {

        public static final Logger log = LoggerFactory.getLogger(ProcessorTask.class);
        private final QueueConnection connection;
        private final ConcurrentLinkedQueue<Object> receivedMessages;
        private AtomicInteger atomicInteger = new AtomicInteger(1);
        private QueueSession queueSession;

        public ProcessorTask(QueueConnection queueConnection, ConcurrentLinkedQueue<Object> receivedMessages) {
            this.connection = queueConnection;
            this.receivedMessages = receivedMessages;

        }

        @Override
        public void run() {
            MessageConsumer consumer = null;
            try {

                queueSession = connection.createQueueSession(true, -1);
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

                        log.debug("==== " + atomicInteger.addAndGet(1) + " ====> " + outboundTransmissionRequest.toString());

                        Path path = Paths.get(outboundTransmissionRequest.getPayloadUri());

                        receivedMessages.add(outboundTransmissionRequest);
                        queueSession.commit();
                    } else if (message != null) {
                        log.error("Received message of unknown type: " + message.getClass().getName());
                    } else
                        log.error("No message received");
                }

            } catch (JMSException e) {
                if (e.getCause() instanceof InterruptedException) {
                    log.info("JMS interrupted, shutting down");
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