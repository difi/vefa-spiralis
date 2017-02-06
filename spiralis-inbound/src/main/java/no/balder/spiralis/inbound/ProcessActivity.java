package no.balder.spiralis.inbound;

import com.google.inject.Inject;
import no.balder.spiralis.payload.PayloadStore;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.sbdh.util.XMLStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author steinar
 *         Date: 04.02.2017
 *         Time: 12.37
 */
class ProcessActivity {

    public static final Logger LOGGER = LoggerFactory.getLogger(ProcessActivity.class);

    public static final int N_THREADS = 4;
    private final LinkedBlockingQueue<SpiralisTask> createdTasksQueue;
    private final PayloadStore payloadStore;
    int threadNumber = 0;
    private ExecutorService executorService;
    private AtomicLong processCount = new AtomicLong(0);

    @Inject
    public ProcessActivity(LinkedBlockingQueue<SpiralisTask> createdTasksQueue, PayloadStore payloadStore) {
        this.createdTasksQueue = createdTasksQueue;
        this.payloadStore = payloadStore;
    }

    public void invoke() {
        startProcessActivity(createdTasksQueue);
    }

    private void startProcessActivity(LinkedBlockingQueue<SpiralisTask> createdTasksQueue) {

        executorService = Executors.newFixedThreadPool(N_THREADS);

        for (int i = 0; i < N_THREADS; i++) {
            executorService.submit(createSpiralisTaskProcessor(createdTasksQueue));
        }
    }

    private Callable<Void> createSpiralisTaskProcessor(LinkedBlockingQueue<SpiralisTask> createdTasksQueue) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (; ; ) {
                    final SpiralisTask spiralisTask = createdTasksQueue.take();
                    final Path path = spiralisTask.getPath();

                    LOGGER.debug("Processing " + spiralisTask);
                    final String fileName = path.getFileName().toString();
                    Header header;
                    if (fileName.endsWith("-doc.xml") || fileName.endsWith("-doc.XML")) {

                        try (final SbdReader sbdReader = SbdReader.newInstance(Files.newInputStream(path))) {

                            // Grabs the header
                            header = sbdReader.getHeader();

                            // Obtains a reader, which will read the embedded xml document
                            final XMLStreamReader xmlStreamReader = sbdReader.xmlReader();

                            final Path tempFile = Files.createTempFile("upload-", ".xml");

                            // Extracts the
                            try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                                XMLStreamUtils.copy(xmlStreamReader, outputStream);
                            }
                        }

                        final Date creationTimestamp = header.getCreationTimestamp();
                        final OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(creationTimestamp.toInstant(), ZoneId.systemDefault());

                        final URI upload = payloadStore.upload(path, header.getSender().toString(), header.getReceiver().toString(), offsetDateTime);
                        processCount.incrementAndGet();
                        LOGGER.debug("Uploaded " + upload);
                    }
                    if (fileName.endsWith(".smime")) {
                        payloadStore.upload(path, null, null, OffsetDateTime.now());
                    }

                }
            }
        };
    }

    public Long getProcessCount() {
        return processCount.get();
    }
}
