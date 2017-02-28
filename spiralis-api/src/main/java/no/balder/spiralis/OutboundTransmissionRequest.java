package no.balder.spiralis;


import no.difi.oxalis.api.model.TransmissionIdentifier;

import java.io.Serializable;
import java.net.URI;

/**
 * @author steinar
 *         Date: 01.12.2016
 *         Time: 12.41
 */
public class OutboundTransmissionRequest implements Serializable {

    final URI payloadUri;
    final TransmissionIdentifier transmissionIdentifier;
    private final boolean validationRequired;
    private final String sender;
    private final String receiver;
    private final String documentTypeId;
    private final String processTypeId;

    public OutboundTransmissionRequest(TransmissionIdentifier transmissionIdentifier, URI payloadUri, boolean validationRequired, String sender, String receiver, String documentTypeId, String processTypeId) {
        this.payloadUri = payloadUri;
        this.transmissionIdentifier = transmissionIdentifier;
        this.validationRequired = validationRequired;
        this.sender = sender;
        this.receiver = receiver;
        this.documentTypeId = documentTypeId;
        this.processTypeId = processTypeId;
    }



    public URI getPayloadUri() {
        return payloadUri;
    }

    public TransmissionIdentifier getTransmissionId() {
        return transmissionIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OutboundTransmissionRequest that = (OutboundTransmissionRequest) o;

        if (!payloadUri.equals(that.payloadUri)) return false;
        return transmissionIdentifier.equals(that.transmissionIdentifier);
    }

    @Override
    public int hashCode() {
        int result = payloadUri.hashCode();
        result = 31 * result + transmissionIdentifier.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OutboundTransmissionRequest{");
        sb.append("payloadUri=").append(payloadUri);
        sb.append(", messageId=").append(transmissionIdentifier);
        sb.append(", validationRequired=").append(validationRequired);
        sb.append(", sender='").append(sender).append('\'');
        sb.append(", receiver='").append(receiver).append('\'');
        sb.append(", documentTypeId='").append(documentTypeId).append('\'');
        sb.append(", processTypeId='").append(processTypeId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public boolean isValidationRequired() {
        return validationRequired;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getDocumentTypeId() {
        return documentTypeId;
    }

    public String getProcessTypeId() {
        return processTypeId;
    }

    public TransmissionIdentifier getTransmissionIdentifier() {
        return transmissionIdentifier;
    }
}
