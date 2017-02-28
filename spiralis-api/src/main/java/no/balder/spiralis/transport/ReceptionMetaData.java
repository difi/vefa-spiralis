package no.balder.spiralis.transport;

import java.sql.Timestamp;

/**
 * @author steinar
 *         Date: 13.02.2017
 *         Time: 08.21
 */
public class ReceptionMetaData {
    private int messageNo;
    private int accountId;
    private String direction;
    private Timestamp received;
    private Timestamp delivered;
    private String sender;
    private String receiver;
    private String channel;
    private String receptionId;
    private String transmissionId;
    private String instanceId;
    private String documentTypeId;
    private String processTypeId;
    private String apName;
    private String payloadUrl;
    private String evidenceUrl;

    public void setMessageNo(int messageNo) {
        this.messageNo = messageNo;
    }

    public int getMessageNo() {
        return messageNo;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

    public void setReceived(Timestamp received) {
        this.received = received;
    }

    public Timestamp getReceived() {
        return received;
    }

    public void setDelivered(Timestamp delivered) {
        this.delivered = delivered;
    }

    public Timestamp getDelivered() {
        return delivered;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiver() {
        return receiver;
    }


    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setReceptionId(String receptionId) {
        this.receptionId = receptionId;
    }

    public String getReceptionId() {
        return receptionId;
    }

    public void setTransmissionId(String transmissionId) {
        this.transmissionId = transmissionId;
    }

    public String getTransmissionId() {
        return transmissionId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setDocumentTypeId(String documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentTypeId() {
        return documentTypeId;
    }

    public void setProcessTypeId(String processTypeId) {
        this.processTypeId = processTypeId;
    }

    public String getProcessTypeId() {
        return processTypeId;
    }

    public void setApName(String apName) {
        this.apName = apName;
    }

    public String getApName() {
        return apName;
    }

    public void setPayloadUrl(String payloadUrl) {
        this.payloadUrl = payloadUrl;
    }

    public String getPayloadUrl() {
        return payloadUrl;
    }

    public void setEvidenceUrl(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReceptionMetaData{");
        sb.append("messageNo=").append(messageNo);
        sb.append(", accountId=").append(accountId);
        sb.append(", direction='").append(direction).append('\'');
        sb.append(", received=").append(received);
        sb.append(", delivered=").append(delivered);
        sb.append(", sender='").append(sender).append('\'');
        sb.append(", receiver='").append(receiver).append('\'');
        sb.append(", channel='").append(channel).append('\'');
        sb.append(", receptionId='").append(receptionId).append('\'');
        sb.append(", transmissionId='").append(transmissionId).append('\'');
        sb.append(", instanceId='").append(instanceId).append('\'');
        sb.append(", documentTypeId='").append(documentTypeId).append('\'');
        sb.append(", processTypeId='").append(processTypeId).append('\'');
        sb.append(", apName='").append(apName).append('\'');
        sb.append(", payloadUrl='").append(payloadUrl).append('\'');
        sb.append(", evidenceUrl='").append(evidenceUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
