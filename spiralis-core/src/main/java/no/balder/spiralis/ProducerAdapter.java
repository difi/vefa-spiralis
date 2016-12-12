package no.balder.spiralis;

/**
 * @author steinar
 *         Date: 07.12.2016
 *         Time: 18.25
 */
public interface ProducerAdapter<T> {

    void send(T msg);
}
