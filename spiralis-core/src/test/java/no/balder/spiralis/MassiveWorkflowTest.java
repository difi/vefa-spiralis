package no.balder.spiralis;

import com.google.inject.Inject;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.SbdhWrapper;
import eu.peppol.identifier.MessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 13.12.2016
 *         Time: 11.27
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class MassiveWorkflowTest {

    public static final Logger log = LoggerFactory.getLogger(MassiveWorkflowTest.class);


    @Inject
    Connection connection;

    @Inject
    ObjectMother objectMother;

    private ExecutorService executorService;


    /**
     * Scans a directory for all XML files and parses each one of them in order to determine the
     * PEPPOL SBDH.
     *
     * @throws Exception
     */
    @Test
    public void testIterateAllMessages() throws Exception {

        int max = 1000;
        long start = System.nanoTime();

        Map<Path, PeppolStandardBusinessHeader> pathPeppolStandardBusinessHeaderMap = scanAndParse(max);
        long end = System.nanoTime();

        long elapsed = end - start;

        System.out.println(max + " entries took " + TimeUnit.SECONDS.convert(elapsed, TimeUnit.NANOSECONDS) + "s");

        long averageInNano = elapsed / max;
        System.out.println("Average time per document " + TimeUnit.MILLISECONDS.convert(averageInNano,TimeUnit.NANOSECONDS) + "ms");
    }


    /** Scans a directory for xml files, parses and extracts a PEPPOL SBDH after which an {@link OutboundTransmissionRequest}
     * is created and posted to a queue. The second part of the test creates a workflow and retrieves the messages
     * from the queue.
     *
     * @throws Exception
     */
    @Test
    public void testParseAndPostOnQueue() throws Exception {

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        ProducerAdapter<OutboundTransmissionRequest> oa = new ProducerAdapterImpl<>(session, Place.OUTBOUND_WORKFLOW_START);

        int max = 1000;
        Map<Path, PeppolStandardBusinessHeader> headerMap = scanAndParse(max);

        log.debug("Now writing entries");

        long start = System.nanoTime();
        long counter = 0;
        for (Map.Entry<Path, PeppolStandardBusinessHeader> entry : headerMap.entrySet()) {

            PeppolStandardBusinessHeader header = entry.getValue();

            OutboundTransmissionRequest outboundTransmissionRequest = new OutboundTransmissionRequest(new MessageId(), entry.getKey().toUri(),
                    false,
                    header.getSenderId().stringValue(),
                    header.getRecipientId().stringValue(),
                    header.getDocumentTypeIdentifier().toString(),
                    header.getProfileTypeIdentifier().toString());

            oa.send(outboundTransmissionRequest);
            counter++;
            if (counter % max == 0) {
                log.debug("Processed " + counter + " entries");
            }
        }
        long end = System.nanoTime();
        System.out.println("Elapsed time for posting " + TimeUnit.SECONDS.convert(end - start, TimeUnit.NANOSECONDS) + "s");


        List<SbdhInspectionTask> tasks = createWorkflow();
        connection.start();

        log.debug("Sleeping ...");

        long processCount = 0L;

        long waits = 0;

        do {
            Thread.sleep(1 * 1000L);
            waits++;
            processCount = 0;
            for (SbdhInspectionTask task : tasks) {
                processCount += task.getProcessCount();
            }

            if (waits > 10) {
                break;
            }
        } while (processCount < max);

        executorService.shutdownNow();

        assertEquals(processCount, max);
    }


    private List<SbdhInspectionTask> createWorkflow() throws JMSException {


        List<SbdhInspectionTask> tasks = new ArrayList<>();

        executorService = Executors.newFixedThreadPool(3);
        for (int i=0; i < 3; i++) {
            Session session = connection.createSession(true, -1);
            SbdhInspectionTask sbdhInspectionTask = new SbdhInspectionTask(session, new ConsumerAdapterImpl<OutboundTransmissionRequest>(session, Place.OUTBOUND_WORKFLOW_START),
                    new ProducerAdapterImpl<OutboundTransmissionRequest>(session, Place.OUTBOUND_TRANSMISSION),
                    new ProducerAdapterImpl<String>(session, new Place("ERROR")), new SbdhWrapper());

            Future<?> submit = executorService.submit(sbdhInspectionTask);
            tasks.add(sbdhInspectionTask);
        }

        return tasks;
    }


    Map<Path, PeppolStandardBusinessHeader> scanAndParse(int max) throws IOException {
        return objectMother.scanAndParse(max);
    }

    private List<Path> getPathsToTestFiles() throws IOException {
        return objectMother.getPathsToTestFiles(Integer.MAX_VALUE);
    }
}
