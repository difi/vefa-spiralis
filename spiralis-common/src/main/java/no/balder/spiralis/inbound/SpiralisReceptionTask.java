package no.balder.spiralis.inbound;

import no.balder.spiralis.payload.WellKnownFileTypeSuffix;
import no.balder.spiralis.transport.ReceptionId;
import no.difi.oxalis.api.inbound.InboundMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * @author steinar
 *         Date: 04.02.2017
 *         Time: 10.42
 */
public class SpiralisReceptionTask {

    public static final Logger LOGGER = LoggerFactory.getLogger(SpiralisReceptionTask.class);

    // Holds the unique identification of this reception
    private final ReceptionId receptionId = new ReceptionId();
    private Path payloadPath;        // the payload
    private Path remEvidencePath;       // the REM evidence
    private final List<Path> associatedFiles;
    private final InboundMetadata inboundMetadata;
    private String sendersApId = "UNKNOWN";


    public SpiralisReceptionTask(List<Path> associatedFiles, InboundMetadata inboundMetadata) {
        this.associatedFiles = associatedFiles;
        this.inboundMetadata = inboundMetadata;

        remEvidencePath = null;
        for (Path path : associatedFiles) {
            if (path.getFileName().toString().endsWith(WellKnownFileTypeSuffix.REM_EVIDENCE.getSuffix())) {
                remEvidencePath = path;
            }

            if (path.getFileName().toString().endsWith(WellKnownFileTypeSuffix.PAYLOAD.getSuffix())) {
                payloadPath = path;
            }
        }

        if (remEvidencePath == null) {
            LOGGER.warn("No REM evidence path found in list of associated files: " + associatedFiles);
        }
        
        if (payloadPath == null) {
            throw new IllegalStateException("No payload file found in list of associated files: " + associatedFiles);
        }
        
        sendersApId = inboundMetadata.getCertificate().getSubjectDN().toString();
    }

    public ReceptionId getReceptionId() {
        return receptionId;
    }


    public Path getPayloadPath() {
        return payloadPath;
    }

    public Path getRemEvidencePath() {
        return remEvidencePath;
    }

    public InboundMetadata getInboundMetadata() {
        return inboundMetadata;
    }

    public List<Path> getAssociatedFiles() {
        return associatedFiles;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpiralisReceptionTask{");
        sb.append("receptionId=").append(receptionId);
        sb.append(", payloadPath=").append(payloadPath);
        sb.append(", inboundMetadata=").append(inboundMetadata);
        sb.append(", sendersApId='").append(sendersApId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getSendersApId() {
        return sendersApId;
    }
}
