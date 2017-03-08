package no.balder.spiralis.payload;

import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.sr.ringo.message.MessageMetaData;
import no.sr.ringo.message.PayloadUriRewriter;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author steinar
 *         Date: 19.02.2017
 *         Time: 19.55
 */
public class AzurePayloadUriRewriter implements PayloadUriRewriter {

    private final AzurePayloadStore azurePayloadStore;

    public AzurePayloadUriRewriter() {
    
        // Loads the Azure connection string
        final Config config = loadConfig();

        if (!config.hasPath("spiralis.azure.connect")) {
            throw new IllegalStateException("Unable to load config param spiralis.azure.connect from ~/.spiralis/spiralis.conf");
        }

        // Creates the Azure Client
        azurePayloadStore = new AzurePayloadStore(config.getString("spiralis.azure.connect"));

    }

    private Config loadConfig() {
        final String homeDirName = System.getProperty("user.home");
        final Path confPath = Paths.get(homeDirName, ".spiralis", "spiralis.conf");
        final Config externalConfig = ConfigFactory.parseFile(confPath.toFile());

        ConfigFactory.invalidateCaches();
        final Config effectiveConfig = ConfigFactory.systemProperties()
                .withFallback(externalConfig)
                .withFallback(ConfigFactory.defaultReference()).resolve();
        return effectiveConfig;
    }


    @Override
    public URI rewrite(URI uriOfBlob) {
        if (uriOfBlob == null) {
            throw new IllegalArgumentException("Required argument URI is null");
        }

        return azurePayloadStore.createUriWithAccessToken(uriOfBlob);
    }
}
