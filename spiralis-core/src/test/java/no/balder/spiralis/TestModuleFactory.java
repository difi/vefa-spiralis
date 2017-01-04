package no.balder.spiralis;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import eu.peppol.persistence.guice.RepositoryModule;
import eu.peppol.persistence.test.TestInMemoryDatabaseModule;
import no.balder.spiralis.guice.JmsModule;
import org.testng.IModuleFactory;
import org.testng.ITestContext;

/**
 * Creates Guice "modules" on the fly for testing purposes.
 *
 * @author steinar
 *         Date: 01.12.2016
 *         Time: 11.55
 */
public class TestModuleFactory implements IModuleFactory {

    static final String EXTERNAL_BROKER_GROUP = "externalBroker";

    @Override
    public Module createModule(ITestContext iTestContext, Class<?> aClass) {

        // Default URL to internal Apache MQ
        String brokerUrl = "vm://localhost?broker.persistent=false";

        if (aClass.getName().equals(AppTest.class.getName())) {
            brokerUrl = "tcp://localhost:61616";
        }


        return new UnitTestModule(brokerUrl);
    }


    class UnitTestModule extends AbstractModule {

        private final String brokerUrl;

        public UnitTestModule(String brokerUrl) {

            this.brokerUrl = brokerUrl;
        }

        @Override
        protected void configure() {

            binder().install(new RepositoryModule());
            binder().install(new TestInMemoryDatabaseModule());
            binder().install(new JmsModule(brokerUrl));
        }
    }


}
