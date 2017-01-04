package no.balder.spiralis;

import eu.peppol.document.SbdhWrapper;
import eu.peppol.outbound.OxalisOutboundComponent;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@link Task} instances
 *
 * @author steinar
 *         Date: 14.12.2016
 *         Time: 18.48
 */
public class TaskFactory {

    private final Connection jmsConnection;
    private final OxalisOutboundComponent oxalisOutboundComponent;

    @Inject
    public TaskFactory(Connection jmsConnection, OxalisOutboundComponent oxalisOutboundComponent) {
        this.jmsConnection = jmsConnection;
        this.oxalisOutboundComponent = oxalisOutboundComponent;
    }

    Connection getJmsConnection() {
        return jmsConnection;
    }

    public List<SbdhInspectionTask> createSbdhInspectionTasks(int qty, Place inputPlace, Place outputPlace, Place errorPlace) throws JMSException {

        List<SbdhInspectionTask> result = new ArrayList<>();
        for (int i = 0; i < qty; i++) {
            Session session = jmsConnection.createSession(true, -1);
            SbdhInspectionTask sbdhInspectionTask = new SbdhInspectionTask(session,
                    AdapterFactory.createConsumerAdapter(session, inputPlace),
                    AdapterFactory.createProducerAdapter(session, outputPlace),
                    AdapterFactory.createProducerAdapter(session, errorPlace),
                    new SbdhWrapper()
            );
            result.add(sbdhInspectionTask);
        }
        return result;
    }

    public List<TransmissionTask> createTransmissionTasks(int qty, Place inputPlace, Place errorPlace) {
        return createTransmissionTasks(qty, null, inputPlace, errorPlace);
    }

    public List<TransmissionTask> createTransmissionTasks(int qty, URL overrideEndPointUrl, Place inputPlace, Place errorPlace) {

        try {
            List<TransmissionTask> tasks = new ArrayList<>();
            for (int i = 0; i < qty; i++) {
                Session session = jmsConnection.createSession(true, -1);
                TransmissionTask transmissionTask = new TransmissionTask(oxalisOutboundComponent,
                        session,
                        AdapterFactory.createConsumerAdapter(session, inputPlace),
                        AdapterFactory.createProducerAdapter(session, errorPlace),
                        overrideEndPointUrl);
                tasks.add(transmissionTask);
            }

            return tasks;
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to create transmission task, reason: " + e.getMessage(), e);
        }
    }

    public List<ValidationTask> createValidatorTasks(int qty, Place inputPlace, Place outputPlace, Place errorPlace) throws JMSException {

        List<ValidationTask> validationTasks = new ArrayList<>();
        for (int i = 0; i < qty; i++) {
            Session session = jmsConnection.createSession(true, -1);
            ValidationTask validationTask = new ValidationTask(session,
                    AdapterFactory.createConsumerAdapter(session, inputPlace),
                    AdapterFactory.createProducerAdapter(session, outputPlace),
                    AdapterFactory.createProducerAdapter(session, errorPlace));
            validationTasks.add(validationTask);
        }
        return validationTasks;
    }
}
