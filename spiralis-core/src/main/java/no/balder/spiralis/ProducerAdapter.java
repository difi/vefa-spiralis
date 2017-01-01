package no.balder.spiralis;

import javax.jms.Session;
import java.io.Serializable;

/**
 * @author steinar
 *         Date: 07.12.2016
 *         Time: 18.25
 */
public interface ProducerAdapter<T extends Serializable> {

    void send(T msg);

    Session getSession();
}
