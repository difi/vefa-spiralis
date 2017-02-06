package no.balder.spiralis.config;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.testng.IModuleFactory;
import org.testng.ITestContext;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 16.32
 */
public class AzureBlobTestModuleFactory implements IModuleFactory {
    @Override
    public Module createModule(ITestContext iTestContext, Class<?> aClass) {

        return new AzureModule();
    }

    class AzureModule extends AbstractModule {

        @Override
        protected void configure() {
            binder().install(new SpiralisConfigurationModule(ConfigFactory.empty()));
        }
    }

    @Provides
    @Named(SpiralisConfigProperty.SPIRALIS_AZURE_CONNECT)
    String azureConnectString(Config config) {
        return config.getString(SpiralisConfigProperty.SPIRALIS_AZURE_ACCOUNT);
    }


}
