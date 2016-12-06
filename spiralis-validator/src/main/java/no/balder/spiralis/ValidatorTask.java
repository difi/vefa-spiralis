package no.balder.spiralis;

import no.difi.vefa.validator.Validator;
import no.difi.vefa.validator.api.Validation;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author steinar
 *         Date: 28.11.2016
 *         Time: 15.53
 */
public class ValidatorTask implements Runnable {

    public static final Logger log = LoggerFactory.getLogger(ValidatorTask.class);
    final Validator validator;
    private final QueueSession queueSession;
    private final MessageConsumer messageConsumer;
    private final MessageProducer messageProducer;
    private final AtomicInteger messageCounter;

    public ValidatorTask(Validator validator, QueueSession queueSession, MessageConsumer messageConsumer, MessageProducer messageProducer, AtomicInteger messageCounter) {
        this.validator = validator;
        this.queueSession = queueSession;
        this.messageConsumer = messageConsumer;
        this.messageProducer = messageProducer;
        this.messageCounter = messageCounter;
    }


    @Override
    public void run() {

        try {
            while (!Thread.currentThread().isInterrupted()) {
                processMessage();
            }
        } catch (JMSException e) {
            log.error("Unable to receive message: " + e.getMessage(), e);
        } catch (IOException e) {
            // Reports that validation failed, but we will continue
            log.error("Validator failed! " + e.getMessage(), e);
            log.info("Processing will contine despite the validator error");
        } finally {
        }

    }

    void processMessage() throws JMSException, IOException {

        log.debug("Waiting for message ...");
        // Reads the next MessageMetaData object from the outbound reception queue
        Message receivedMessage = messageConsumer.receive();

        // Transforms the JMS message into a type known to our domain

        // TODO: enable this later.
        //  OutboundTransmissionRequest outboundTransmissionRequest = MapMessageTransformer.valueOf(receivedMessage);

        // log.debug("Message #" + messageCounter.get() + " received: " + outboundTransmissionRequest);

        // Only validate if requested to do so
        // TODO: Activate code later
/*
        if (outboundTransmissionRequest.isValidationRequired()) {
            log.debug("Validation requested...");
            Path path = Paths.get(outboundTransmissionRequest.getPayloadUri());
            Validation validation = validator.validate(path);

            // Inspects the outcome of the validation
            Report report = validation.getReport();
            if (report.getFlag() == FlagType.FATAL ||
                    report.getFlag() == FlagType.ERROR) {

                log.warn("Validation failed for " + outboundTransmissionRequest);
                // Validation failed, this message can not be transmitted
                // TODO: post message to queue "validation.failed"
                queueSession.commit(); // Prevents this message from occuring again
                throw new UnsupportedOperationException("No support for processing of messages that fail validation");
            } else {

                log.debug("Validation ok, posting on transmission queue");
                // Validation was ok, place on the next queue in our pipeline.
                messageProducer.send(receivedMessage);
            }
        }
*/
        int i = messageCounter.incrementAndGet();
        log.debug("Processed task #" + i);

        // Commits our message consumption
        queueSession.commit();
    }
}
