package no.balder.spiralis;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.persistence.guice.RepositoryModule;
import eu.peppol.persistence.test.TestInMemoryDatabaseModule;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.testng.IModuleFactory;
import org.testng.ITestContext;

import javax.jms.Connection;
import javax.jms.JMSException;

/**
 * @author steinar
 *         Date: 01.12.2016
 *         Time: 11.55
 */
public class TestModuleFactory implements IModuleFactory {
    @Override
    public Module createModule(ITestContext iTestContext, Class<?> aClass) {
        return new InMemoryDatabaseModule();
    }

    class InMemoryDatabaseModule extends AbstractModule {

        @Override
        protected void configure() {
            binder().install(new RepositoryModule());
            binder().install(new TestInMemoryDatabaseModule());
        }


        @Singleton
        @Provides
        protected ActiveMQConnectionFactory provideActiveMQConnectionFactory() {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
            factory.setTrustAllPackages(true);

            return factory;
        }

        @Provides
        protected Connection provideJmsConnection(ActiveMQConnectionFactory connectionFactory) {
            try {
                return connectionFactory.createConnection();
            } catch (JMSException e) {
                throw new IllegalStateException("Unable to create connection " + e.getMessage(), e);
            }
        }
    }
}
