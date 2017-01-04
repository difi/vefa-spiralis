package no.balder.spiralis;

/** Declares a Petri net "inputPlace" from which tokens may be obtained.
 *
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 11.06
 */
public class Place {

    /** Name of queue from which new transmission requests are obtained */
    public static final Place OUTBOUND_WORKFLOW_START = new Place("Outbound.workflow");

    /** Name of queue holding requests for validation */
    public static final Place OUTBOUND_VALIDATION = new Place("Outbound.validation");

    public static final Place OUTBOUND_INSPECTION_ERROR = new Place("Outbound.inspection.error");

    /** Name of queue holding requests for custom task processing */
    public static final Place OUTBOUND_CUSTOM = new Place("Outbound.custom");

    /** Name of queue holding transmission requests ready to be transmitted into the PEPPOL network */
    public static final Place OUTBOUND_TRANSMISSION = new Place("Outbound.transmission");

    public static final Place OUTBOUND_TRANSMISSION_ERROR = new Place("Outbound.transmission.error");

    public static final Place OUTBOUND_VALIDATION_ERROR = new Place("Outbound.validation.error");

    private final String queueName;

    Place(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }

}
