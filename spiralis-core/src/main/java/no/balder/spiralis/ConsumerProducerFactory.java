package no.balder.spiralis;

import javax.jms.QueueSession;

/**
 * @author steinar
 *         Date: 07.12.2016
 *         Time: 20.25
 */
class ConsumerProducerFactory {

    private final QueueSession queueSession;

    public ConsumerProducerFactory(QueueSession queueSession) {
        this.queueSession = queueSession;
    }

}
