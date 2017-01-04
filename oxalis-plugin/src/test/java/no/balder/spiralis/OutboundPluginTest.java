package no.balder.spiralis;

import eu.peppol.identifier.MessageId;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Unit test for simple OutboundPlugin.
 */
public class OutboundPluginTest {


    @Test
    public void testOutboundMessage() throws Exception {

        OutboundPlugin instance = OutboundPlugin.getInstance();


        OutboundTransmissionRequest outboundTransmissionRequest = new OutboundTransmissionRequest(new MessageId(), Paths.get("/tmp/tull").toUri(),
                false, "9908:976098897", "9908:976098897",
                "Dumbo document type", "dumbo process type id");
        instance.makeOutboundRequest(outboundTransmissionRequest);
    }

    @Test
    public void testMultiple() throws Exception {

        Callable<Object> r = () -> {

            for (int i = 0; i < 10; i++) {
                OutboundPlugin instance = OutboundPlugin.getInstance();
                OutboundTransmissionRequest outboundTransmissionRequest = new OutboundTransmissionRequest(new MessageId(), Paths.get("/tmp/tull").toUri(),
                        false, "9908:976098897", "9908:976098897",
                        "Dumbo document type", "dumbo process type id");
                instance.makeOutboundRequest(outboundTransmissionRequest);
            }
            return null;
        };

        List<Callable<Object>> callables = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            callables.add(
                    () -> {

                for (int n = 0; n < 10; n++) {
                    OutboundPlugin instance = OutboundPlugin.getInstance();
                    OutboundTransmissionRequest outboundTransmissionRequest = new OutboundTransmissionRequest(new MessageId(), Paths.get("/tmp/tull").toUri(),
                            false, "9908:976098897", "9908:976098897",
                            "Dumbo document type", "dumbo process type id");
                    instance.makeOutboundRequest(outboundTransmissionRequest);
                }
                return null;
            });
        }

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        executorService.invokeAll(callables);
    }
}
