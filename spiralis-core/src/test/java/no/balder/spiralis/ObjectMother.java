package no.balder.spiralis;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.NoSbdhParser;
import eu.peppol.identifier.MessageId;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 10.41
 */
public class ObjectMother {

    public static final Logger log = LoggerFactory.getLogger(ObjectMother.class);


    public static final String SAMPLE_INVOICE_RESOURCE_NAME = "hafslund-test-1.xml";
    private final Connection jmsConnection;

    @Inject
    public ObjectMother(Connection jmsConnection) {
        this.jmsConnection = jmsConnection;
    }

    public static URL sampleInvoice() {
        URL resource = ObjectMother.class.getClassLoader().getResource(SAMPLE_INVOICE_RESOURCE_NAME);

        assertNotNull(resource, "Unable to locate " + SAMPLE_INVOICE_RESOURCE_NAME + " in classpath");
        return resource;
    }

    public static URI sampleInvoiceURI() {
        try {
            return sampleInvoice().toURI();

        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to convert URL to UIR " + e.getMessage(), e);
        }
    }

    public static InputStream sampleInvoiceStream() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(sampleInvoice().toURI()));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("File not found " + e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URI " + e.getMessage(), e);
        }

        assertNotNull(fileInputStream);
        return fileInputStream;
    }


    public static PeppolStandardBusinessHeader parseSample() {
        URL url = sampleInvoice();

        NoSbdhParser noSbdhParser = new NoSbdhParser();
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = noSbdhParser.parse(sampleInvoiceStream());
        return peppolStandardBusinessHeader;
    }

    @NotNull
    public static OutboundTransmissionRequest sampleOutboundTransmissionRequest() {
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = ObjectMother.parseSample();

        return new OutboundTransmissionRequest(new MessageId(),
                sampleInvoiceURI(),
                true,
                peppolStandardBusinessHeader.getSenderId().stringValue(),
                peppolStandardBusinessHeader.getRecipientId().stringValue(),
                peppolStandardBusinessHeader.getDocumentTypeIdentifier().toString(),
                peppolStandardBusinessHeader.getProfileTypeIdentifier().toString());
    }


    /**
     * Transforms a Map of paths and {@link PeppolStandardBusinessHeader} entries into a list of {@link OutboundTransmissionRequest} objects.
     *
     * @param entries
     * @return
     */
    public List<OutboundTransmissionRequest> toPeppolSbdh(Map<Path, PeppolStandardBusinessHeader> entries) {

        List<OutboundTransmissionRequest> result = new ArrayList<OutboundTransmissionRequest>();

        for (Map.Entry<Path, PeppolStandardBusinessHeader> entry : entries.entrySet()) {
            PeppolStandardBusinessHeader header = entry.getValue();

            OutboundTransmissionRequest transmissionRequest = new OutboundTransmissionRequest(new MessageId(), entry.getKey().toUri(), false, header.getSenderId().stringValue(), header.getRecipientId().stringValue(), header.getDocumentTypeIdentifier().toString(),
                    header.getProfileTypeIdentifier().toString());
            result.add(transmissionRequest);

        }
        return result;
    }

    /**
     * Loads, parses and posts a number of identical sample {@link OutboundTransmissionRequest} instances
     *
     * @param count
     */
    public void postIdenticalSampleInvoicesToOutboundReceptionQueue(int count) {


        OutboundTransmissionRequest outboundTransmissionRequest = sampleOutboundTransmissionRequest();

        try {
            Session queueSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ProducerAdapter<OutboundTransmissionRequest> producerAdapter = AdapterFactory.createProducerAdapter(queueSession, Place.OUTBOUND_WORKFLOW_START);

            try {
                for (int i = 0; i < count; i++) {
                    producerAdapter.send(outboundTransmissionRequest);

                }
            } finally {
                queueSession.close();
            }


        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void postIdenticalSampleInvoicesToOutboundTransmissionQueue(int count) {
        OutboundTransmissionRequest outboundTransmissionRequest = sampleOutboundTransmissionRequest();

        try {
            Session queueSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ProducerAdapter<OutboundTransmissionRequest> producerAdapter = AdapterFactory.createProducerAdapter(queueSession, Place.OUTBOUND_TRANSMISSION);


            try {
                for (int i = 0; i < count; i++) {
                    producerAdapter.send(outboundTransmissionRequest);
                }
            } finally {
                queueSession.close();
            }


        } catch (JMSException e) {
            throw new IllegalStateException("Unable to perform JMS operation: " + e.getMessage(), e);
        }
    }


    public List<OutboundTransmissionRequest> postSampleInvoices(int max, Place place) {
        Map<Path, PeppolStandardBusinessHeader> samples = null;
        try {
            // Scans the sample directory and loads bunch of samples
            samples = scanAndParse(max);

            // Transforms them into OutboundTransmissionRequests
            List<OutboundTransmissionRequest> outboundTransmissionRequests = toPeppolSbdh(samples);

            // Posts them to the outbound reception queue
            Session writeSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ProducerAdapter<OutboundTransmissionRequest> producerAdapter = AdapterFactory.createProducerAdapter(writeSession, place);
            for (OutboundTransmissionRequest request : outboundTransmissionRequests) {
                log.debug("Posting messageId " + request.getMessageId());
                producerAdapter.send(request);
            }
            writeSession.close();

            return outboundTransmissionRequests;

        } catch (IOException | JMSException e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * Scans the /tmp/Out directory and provides a bunch of sample files
     *
     * @return
     * @throws IOException
     */
    @NotNull
    public List<Path> getPathsToTestFiles(int max) throws IOException {
        List<Path> dirListing = new ArrayList<>();

        Path directoryWithSampleFiles = Paths.get("/tmp/Out");
        if (!Files.isDirectory(directoryWithSampleFiles)) {
            throw new IllegalStateException("Directory '" + directoryWithSampleFiles.toString() + "' does not exist");
        }

        int count = 0;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directoryWithSampleFiles, "*.{XML,xml}")) {
            for (Path path : paths) {
                dirListing.add(path);
                count++;
                if (count >= max) {
                    break;
                }
            }
        }
        return dirListing;
    }

    /**
     * Scans the directory holding the sample files, parses them in order to produce a PEPPOL variant of the SBDH
     *
     * @param max max number of entries to produce
     * @return map holding the {@link Path} as the key and the {@link PeppolStandardBusinessHeader} as the value.
     * @throws IOException
     */
    public Map<Path, PeppolStandardBusinessHeader> scanAndParse(int max) throws IOException {

        // Limits the number of entries in accordance with the supplied upper bound
        List<Path> dirListing = getPathsToTestFiles(max);

        NoSbdhParser noSbdhParser = new NoSbdhParser();

        Map<Path, PeppolStandardBusinessHeader> headers = new HashMap<>();

        int counter = 0;
        for (Path path : dirListing) {

            try (InputStream inputStream = Files.newInputStream(path)) {

                PeppolStandardBusinessHeader header = noSbdhParser.parse(inputStream);
                headers.put(path, header);
                counter++;
                if (counter % 1000 == 0) {
                    log.debug("Added header for " + counter + " entries");
                }
            }
        }
        return headers;
    }
}
