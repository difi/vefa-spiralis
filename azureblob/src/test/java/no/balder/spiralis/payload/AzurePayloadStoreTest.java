package no.balder.spiralis.payload;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import no.balder.spiralis.TestResources;
import no.balder.spiralis.config.SpiralisConfigProperty;
import no.balder.spiralis.config.AzureBlobTestModuleFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;

import static no.balder.spiralis.config.SpiralisConfigProperty.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 16.08
 */
@Guice(moduleFactory = AzureBlobTestModuleFactory.class)
public class AzurePayloadStoreTest {

    @Inject
    Config config;


    @BeforeMethod
    public void setUp() throws Exception {
        assertNotNull(config);
        assertTrue(config.hasPath(SPIRALIS_AZURE_ACCOUNT));

        assertTrue(config.hasPath("spiralis.azure.connect"));
    }


    @Test
    public void uploadSampleFile() throws Exception {

        final URL resource = AzurePayloadStoreTest.class.getClassLoader().getResource(TestResources.SAMPLE_INVOICE_XML);

        final URI uri = resource.toURI();
        final Path path = Paths.get(uri);
        System.out.println(path);


        final AzurePayloadStore azurePayloadStore = new AzurePayloadStore(config.getString(SPIRALIS_AZURE_CONNECT));


        final URI upload = azurePayloadStore.upload(path, "sampleblob");
        System.out.println(upload);

    }
}