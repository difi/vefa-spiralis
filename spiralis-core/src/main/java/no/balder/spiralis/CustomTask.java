package no.balder.spiralis;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author steinar
 *         Date: 14.12.2016
 *         Time: 16.21
 */
public class CustomTask extends AbstractTask {
    public CustomTask(Session queueSession) {
        super(queueSession);
    }

    @Override
    void processNextInputItem() throws JMSException {

    }
}
