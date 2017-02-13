package no.balder.spiralis;

import eu.peppol.PeppolStandardBusinessHeader;
// import eu.peppol.document.SbdhWrapper;
import eu.peppol.identifier.InstanceId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
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
   // private final SbdhWrapper sbdhWrapper;

    private final AtomicLong processCount = new AtomicLong(0);

    public SbdhInspectionTask(Session queueSession,
                              ConsumerAdapter<OutboundTransmissionRequest> consumerAdapter,
                              ProducerAdapter<OutboundTransmissionRequest> producerAdapter,
                              ProducerAdapter<String> errorProducer //,
    //                          SbdhWrapper sbdhWrapper
    ) {
        super(queueSession);

        this.consumerAdapter = consumerAdapter;
        this.producerAdapter = producerAdapter;
        this.errorProducer = errorProducer;
     //    this.sbdhWrapper = sbdhWrapper;
    }

    @Override
    void processNextInputItem() throws JMSException, InterruptedException {

        OutboundTransmissionRequest request = consumerAdapter.receive();

        log.info("Wrapping with SBDH is postponed until transport for " + request.getMessageId());

        producerAdapter.send(request);

        long counter = processCount.incrementAndGet();

        log.debug("Processed #" + counter + " " + request);
    }

    public long getProcessCount() {
        return processCount.get();
    }
}
