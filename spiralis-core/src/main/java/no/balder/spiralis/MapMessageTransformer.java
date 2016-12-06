package no.balder.spiralis;

import eu.peppol.identifier.MessageId;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.QueueSession;
import java.net.URI;

/**
 * @author steinar
 *         Date: 01.12.2016
 *         Time: 15.26
 */
public class MapMessageTransformer {

    public static final String PAYLOAD_URL = "payload_url";
    public static final String MESSAGE_ID_PROPERTY_NAME = "message_id";
    public static final String SENDER_PROPERTY_NAME = "sender";
    public static final String RECEIVER_PROPERTY_NAME = "receiver";
    public static final String DOCUMENT_TYPE_ID_PROPERTY_NAME = "document_type_id";
    public static final String PROCESS_TYPE_ID_PROPERTY_NAME = "process_type_id";
    public static final String VALIDATION_REQUIRED = "validation_required";


    public static OutboundTransmissionRequest valueOf(MapMessage mapMessage) {
        String urlStringAsString;
        String messageIdAsString;
        String sender;
        String receiver;
        String documentTypeId;
        String processTypeId;
        boolean validationRequired = false;

        try {
            urlStringAsString = mapMessage.getString(PAYLOAD_URL);
            messageIdAsString = mapMessage.getString(MESSAGE_ID_PROPERTY_NAME);
            sender = mapMessage.getString(SENDER_PROPERTY_NAME);
            receiver = mapMessage.getString(RECEIVER_PROPERTY_NAME);
            documentTypeId = mapMessage.getString(DOCUMENT_TYPE_ID_PROPERTY_NAME);
            processTypeId = mapMessage.getString(PROCESS_TYPE_ID_PROPERTY_NAME);
            validationRequired = mapMessage.getBoolean(VALIDATION_REQUIRED);

        } catch (JMSException e) {
            throw new IllegalStateException("Unable to retrieve values from MapMessage: " + e.getMessage(), e);
        }
        if (urlStringAsString == null) {
            throw new IllegalArgumentException(PAYLOAD_URL + " has no value in JMS MapMessage");
        }
        if (messageIdAsString == null) {
            throw new IllegalArgumentException(MESSAGE_ID_PROPERTY_NAME + " has no value in JMS MapMessage");
        }
        if (sender == null) {
            throw new IllegalArgumentException("Property " + SENDER_PROPERTY_NAME + " required");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Property " + RECEIVER_PROPERTY_NAME + " required");
        }
        if (documentTypeId == null) {
            throw new IllegalArgumentException("Property " + DOCUMENT_TYPE_ID_PROPERTY_NAME + " required");
        }

        if (processTypeId == null) {
            throw new IllegalArgumentException("Property " + PROCESS_TYPE_ID_PROPERTY_NAME + " required");
        }

        MessageId messageId = new MessageId(messageIdAsString);
        URI payloadUri = null;
        payloadUri = URI.create(urlStringAsString);

        OutboundTransmissionRequest outboundTransmissionRequest = new OutboundTransmissionRequest(messageId, payloadUri, validationRequired, sender, receiver, documentTypeId, processTypeId);
        return outboundTransmissionRequest;
    }


    public static MapMessage from(MapMessage mapMessage, OutboundTransmissionRequest or) {
        try {
            mapMessage.setString(PAYLOAD_URL, or.getPayloadUri().toString());
            mapMessage.setString(MESSAGE_ID_PROPERTY_NAME, or.getMessageId().toString());
            mapMessage.setString(SENDER_PROPERTY_NAME, or.getSender());
            mapMessage.setString(RECEIVER_PROPERTY_NAME, or.getReceiver());
            mapMessage.setString(DOCUMENT_TYPE_ID_PROPERTY_NAME, or.getDocumentTypeId());
            mapMessage.setString(PROCESS_TYPE_ID_PROPERTY_NAME, or.getProcessTypeId());
            mapMessage.setBoolean(VALIDATION_REQUIRED, or.isValidationRequired());
            return mapMessage;
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to create new MapMessage: " + e.getMessage(), e);
        }
    }

    public static OutboundTransmissionRequest valueOf(Message receivedMessage) {
        if (receivedMessage == null) {
            throw new IllegalArgumentException("Message argument required");
        }

        if (receivedMessage instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) receivedMessage;
            return valueOf(mapMessage);
        } else
            throw new IllegalStateException("Unable to create MapMessage from " + receivedMessage.getClass().getName());
    }
}
