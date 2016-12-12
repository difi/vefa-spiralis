package no.balder.spiralis.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.lang.IllegalStateException;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 15.04
 */
public class JmsModule extends AbstractModule
{
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    protected QueueConnection provideActiveMQConnection(ActiveMQConnectionFactory connectionFactory) {

        try {
            return connectionFactory.createQueueConnection();
        } catch (JMSException e) {
            System.err.println("Unable to create QueueConnection: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    @Provides
    @Singleton
    protected ActiveMQConnectionFactory provideActiveMQConnectionFactory() {
        return new ActiveMQConnectionFactory();
    }

}
