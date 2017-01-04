package no.balder.spiralis.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import no.balder.spiralis.AdapterFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.net.URL;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 15.04
 */
public class JmsModule extends AbstractModule
{
    private ActiveMQConnectionFactory factory;


    public JmsModule(String brokerUrl) {
        factory = new ActiveMQConnectionFactory(brokerUrl);
        factory.setTrustAllPackages(true);
    }


    @Override
    protected void configure() {
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