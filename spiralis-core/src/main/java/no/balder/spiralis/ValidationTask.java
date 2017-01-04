package no.balder.spiralis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author steinar
 *         Date: 14.12.2016
 *         Time: 16.21
 */
public class ValidationTask extends AbstractTask {


    public static final Logger log = LoggerFactory.getLogger(ValidationTask.class);

    private final ConsumerAdapter<OutboundTransmissionRequest> consumerAdapter;
    private final ProducerAdapter<OutboundTransmissionRequest> producerAdapter;
    private final ProducerAdapter<String> errorProducer;

    public ValidationTask(Session queueSession,
                          ConsumerAdapter<OutboundTransmissionRequest> consumerAdapter,
                          ProducerAdapter<OutboundTransmissionRequest> producerAdapter,
                          ProducerAdapter<String> errorProducer
    ) {
        super(queueSession);

        this.consumerAdapter = consumerAdapter;
        this.producerAdapter = producerAdapter;
        this.errorProducer = errorProducer;
    }

    @Override
    void processNextInputItem() throws JMSException, InterruptedException {
        OutboundTransmissionRequest request = consumerAdapter.receive();

        log.info("Skipping the validation for " + request.getMessageId());

        producerAdapter.send(request);
    }
}
