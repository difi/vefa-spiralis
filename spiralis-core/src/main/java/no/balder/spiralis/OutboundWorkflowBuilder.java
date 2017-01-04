package no.balder.spiralis;

import com.google.inject.Inject;

import javax.jms.Connection;
import java.util.List;

/**
 * Builds the default outbound workflow consisting of only the transmission tasks.
 *
 * @author steinar
 *         Date: 04.01.2017
 *         Time: 10.17
 */
public class OutboundWorkflowBuilder {


    private final Connection connection;
    private final TransmissionTaskBuilder transmissionTaskBuilder;


    private List<TransmissionTask> tasks;

    private String overrrideUrl = null;     // Default is to not override
    private int transmissionTaskInstances = 1;              // Default is to have a single instance

    @Inject
    public OutboundWorkflowBuilder(Connection connection, TransmissionTaskBuilder transmissionTaskBuilder) {
        this.connection = connection;
        this.transmissionTaskBuilder = transmissionTaskBuilder;
    }


    OutboundWorkflowBuilder transmissionEndPoint(String overrrideUrl) {
        this.overrrideUrl = overrrideUrl;
        return this;
    }


    OutboundWorkflowBuilder trasmissionTaskCount(int instances) {
        this.transmissionTaskInstances = instances;
        return this;
    }

    public OutboundWorkflow build() {

        OutboundWorkflow outboundWorkflow = new OutboundWorkflow(connection);


        List<TransmissionTask> transmissionTasks = transmissionTaskBuilder.errorPlace(Place.OUTBOUND_TRANSMISSION_ERROR)
                .inputPlace(Place.OUTBOUND_WORKFLOW_START)
                .overrideEndpointUrl(overrrideUrl)
                .build(transmissionTaskInstances);

        Transaction transmissionTx = outboundWorkflow.addTransmissionTransaction(transmissionTasks);

        return outboundWorkflow;

    }
}
