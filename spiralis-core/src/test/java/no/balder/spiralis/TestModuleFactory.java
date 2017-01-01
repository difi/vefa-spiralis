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

import javax.jms.*;
import java.lang.IllegalStateException;

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


        private ActiveMQConnectionFactory factory;

        @Override
        protected void configure() {
            factory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
            factory.setTrustAllPackages(true);

            binder().install(new RepositoryModule());
            binder().install(new TestInMemoryDatabaseModule());

            bind(AdapterFactory.class);
        }


        @Provides
        @Singleton
        protected ConnectionFactory provideActiveMQConnectionFactory() {
            return factory;
        }

        @Provides
        @javax.inject.Singleton
        protected Connection provideJmsConnection(ConnectionFactory connectionFactory) {
            try {
                return connectionFactory.createConnection();
            } catch (JMSException e) {
                throw new IllegalStateException("Unable to create connection " + e.getMessage(), e);
            }
        }

        /** Provides a Transactional JMS session
         *
         * @param connection
         * @return
         */
        @Provides
        protected Session provideTxJmsSession(Connection connection) {
            try {
                return connection.createSession(true, -1);
            } catch (JMSException e) {
                throw new IllegalStateException("Unable to create JMS session " + e.getMessage(), e);
            }
        }
    }
}
