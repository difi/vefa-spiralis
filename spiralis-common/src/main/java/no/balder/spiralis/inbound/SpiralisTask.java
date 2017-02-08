package no.balder.spiralis.inbound;

import no.difi.vefa.peppol.common.model.Header;

import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;

/**
 * @author steinar
 *         Date: 04.02.2017
 *         Time: 10.42
 */
public class SpiralisTask {
    private final Path payloadPath;        // the payload
    private final Header header;    // SBDH header
    private Path smimePath;         // the transmission receipt
    private String transmissionId;  // the transmission-ID retrieved from the receipt
    private TemporalAccessor received;
    private String ourMessageId;
    private String sendersApId = "UNKNOWN";


    public SpiralisTask(Path payloadPath, Header header) {

        this.payloadPath = payloadPath;
        this.header = header;
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
        final StringBuilder sb = new StringBuilder("SpiralisTask{");
        sb.append("payloadPath=").append(payloadPath);
        sb.append(", header=").append(header);
        sb.append(", smimePath=").append(smimePath);
        sb.append(", transmissionId='").append(transmissionId).append('\'');
        sb.append(", received=").append(received);
        sb.append('}');
        return sb.toString();
    }

    public void setOurMessageId(String ourMessageId) {
        this.ourMessageId = ourMessageId;
    }

    public String getOurMessageId() {
        return ourMessageId;
    }

    public void setSendersApId(String sendersApId) {
        this.sendersApId = sendersApId;
    }

    public String getSendersApId() {
        return sendersApId;
    }
}
