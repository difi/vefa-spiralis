package no.balder.spiralis;

/**
 * Adapter for JMS {@link javax.jms.MessageConsumer} instances
 *
 * The generic indicates the type of messages to be consumed.
 *
 * @param <T> type of message to be consumed
 *
 * @author steinar
 *         Date: 07.12.2016
 *         Time: 14.17
 */
public interface ConsumerAdapter <T> {

    T receive();
}
