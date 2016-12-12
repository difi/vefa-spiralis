package no.balder.spiralis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author steinar
 *         Date: 07.12.2016
 *         Time: 15.51
 */
public class SbdhInspectionTask extends AbstractTask {

    public static final Logger log = LoggerFactory.getLogger(SbdhInspectionTask.class);

    private final ConsumerAdapter<OutboundTransmissionRequest> consumerAdapter;
    private final ProducerAdapter<OutboundTransmissionRequest> producerAdapter;
    private final ProducerAdapter<String> errorProducer;

    private final AtomicLong processCount = new AtomicLong(0);

    public SbdhInspectionTask(Session queueSession, ConsumerAdapter<OutboundTransmissionRequest> consumerAdapter, ProducerAdapter<OutboundTransmissionRequest> producerAdapter, ProducerAdapter<String> errorProducer) {
        super(queueSession);

        this.consumerAdapter = consumerAdapter;
        this.producerAdapter = producerAdapter;
        this.errorProducer = errorProducer;
    }

    @Override
    void processNextInputItem() throws JMSException {
        OutboundTransmissionRequest request = consumerAdapter.receive();

        log.debug("Processing " + request);

        producerAdapter.send(request);

        commit();

        processCount.incrementAndGet();
    }

    public long getProcessCount() {
        return processCount.get();
    }
}
