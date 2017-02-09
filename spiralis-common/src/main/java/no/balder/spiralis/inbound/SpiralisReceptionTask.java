package no.balder.spiralis.inbound;

import no.difi.vefa.peppol.common.model.Header;

import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;

/**
 * @author steinar
 *         Date: 04.02.2017
 *         Time: 10.42
 */
public class SpiralisReceptionTask {

    // Holds the unique identification of this reception
    private final UUID receptionId = UUID.randomUUID();

    private final Path payloadPath;        // the payload
    private final Header header;    // SBDH header
    private Path smimePath;         // the transmission receipt
    private String transmissionId;  // the transmission-ID retrieved from the receipt
    private TemporalAccessor received;
    private String oxalisMessageId;
    private String sendersApId = "UNKNOWN";


    public SpiralisReceptionTask(Path payloadPath, Header header) {

        this.payloadPath = payloadPath;
        this.header = header;
    }

    public UUID getReceptionId() {
        return receptionId;
    }

    public Path getPayloadPath() {
        return payloadPath;
    }

    public Header getHeader() {
        return header;
    }


    public void setSmimePath(Path smimePath) {
        this.smimePath = smimePath;
    }

    public Path getSmimePath() {
        return smimePath;
    }

    public void setTransmissionId(String transmissionId) {
        this.transmissionId = transmissionId;
    }

    public String getTransmissionId() {
        return transmissionId;
    }

    public TemporalAccessor getReceived() {
        return received;
    }

    public void setReceived(TemporalAccessor received) {
        this.received = received;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpiralisReceptionTask{");
        sb.append("receptionId=").append(receptionId);
        sb.append(", payloadPath=").append(payloadPath);
        sb.append(", header=").append(header);
        sb.append(", smimePath=").append(smimePath);
        sb.append(", transmissionId='").append(transmissionId).append('\'');
        sb.append(", received=").append(received);
        sb.append(", oxalisMessageId='").append(oxalisMessageId).append('\'');
        sb.append(", sendersApId='").append(sendersApId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void setOxalisMessageId(String oxalisMessageId) {
        this.oxalisMessageId = oxalisMessageId;
    }

    public String getOxalisMessageId() {
        return oxalisMessageId;
    }

    public void setSendersApId(String sendersApId) {
        this.sendersApId = sendersApId;
    }

    public String getSendersApId() {
        return sendersApId;
    }
}
