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

    List<SbdhInspectionTask> createSbdhInspectionTasks(int qty) throws JMSException {

        List<SbdhInspectionTask> result = new ArrayList<>();
        for (int i = 0; i < qty; i++) {
            Session session = jmsConnection.createSession(true, -1);
            SbdhInspectionTask sbdhInspectionTask = new SbdhInspectionTask(session,
                    AdapterFactory.createConsumerAdapter(session, Place.OUTBOUND_WORKFLOW),
                    AdapterFactory.createProducerAdapter(session, Place.OUTBOUND_VALIDATION),
                    AdapterFactory.createProducerAdapter(session, Place.OUTBOUND_INSPECTION_ERROR),
                    new SbdhWrapper()
            );
            result.add(sbdhInspectionTask);
        }
        return result;
    }

    List<TransmissionTask> createTransmissionTasks(int qty) throws JMSException {
        return createTransmissionTasks(qty, null);
    }

    List<TransmissionTask> createTransmissionTasks(int qty, URL overrideEndPointUrl) throws JMSException {

        List<TransmissionTask> tasks = new ArrayList<>();
        for (int i = 0; i < qty; i++) {
            Session session = jmsConnection.createSession(true, -1);
            TransmissionTask transmissionTask = new TransmissionTask(oxalisOutboundComponent, session, AdapterFactory.createConsumerAdapter(session, Place.OUTBOUND_TRANSMISSION), overrideEndPointUrl);
            tasks.add(transmissionTask);
        }

        return tasks;
    }

    public List<ValidationTask> createValidatorTasks(int qty) throws JMSException {

        List<ValidationTask> validationTasks = new ArrayList<>();
        for (int i = 0; i < qty; i++) {
            Session session = jmsConnection.createSession(true, -1);
            ValidationTask validationTask = new ValidationTask(session,
                    AdapterFactory.createConsumerAdapter(session, Place.OUTBOUND_VALIDATION),
                    AdapterFactory.createProducerAdapter(session, Place.OUTBOUND_TRANSMISSION),
                    AdapterFactory.createProducerAdapter(session, Place.OUTBOUND_VALIDATION_ERROR));
            validationTasks.add(validationTask);
        }
        return validationTasks;
    }
}
