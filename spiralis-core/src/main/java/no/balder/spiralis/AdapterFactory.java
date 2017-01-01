package no.balder.spiralis;

import com.google.inject.Inject;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.Serializable;

/**
 * @author steinar
 *         Date: 14.12.2016
 *         Time: 16.27
 */
public class AdapterFactory {


    static <T extends Serializable> ConsumerAdapter<T> createConsumerAdapter(Session session, Place place) {
        return new ConsumerAdapterImpl<T>(session, place);
    }

    static <T extends Serializable> ProducerAdapter<T> createProducerAdapter(Session session, Place place) {
        return new ProducerAdapterImpl<T>(session, place);
    }

}
