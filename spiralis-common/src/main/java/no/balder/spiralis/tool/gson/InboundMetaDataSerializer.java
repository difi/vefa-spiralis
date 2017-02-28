package no.balder.spiralis.tool.gson;

import com.google.gson.*;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.vefa.peppol.common.model.Header;

import java.lang.reflect.Type;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

class InboundMetaDataSerializer implements JsonSerializer<InboundMetadata> {

        @Override
        public JsonElement serialize(InboundMetadata inboundMetadata, Type type, JsonSerializationContext context) {
            final JsonObject jsonObject = new JsonObject();
            final JsonElement serializedMessageId = context.serialize(inboundMetadata.getTransmissionIdentifier(), TransmissionIdentifier.class);

            jsonObject.add("messageId", serializedMessageId);
            jsonObject.add("header", context.serialize(inboundMetadata.getHeader(), Header.class));
            jsonObject.add("timeStamp", context.serialize(inboundMetadata.getTimestamp()));
            jsonObject.add("digest", context.serialize(inboundMetadata.getDigest()));
            jsonObject.add("transportProtocol", context.serialize(inboundMetadata.getTransportProtocol()));
            jsonObject.add("transportProfile", context.serialize(inboundMetadata.getProtocol()));
            jsonObject.add("receipts", context.serialize(inboundMetadata.getReceipts()));
            jsonObject.add("primaryReceipt", context.serialize(inboundMetadata.primaryReceipt()));

            final X509Certificate certificate = inboundMetadata.getCertificate();
            try {
                final String b64Certificate = Base64.getEncoder().encodeToString(certificate.getEncoded());
                jsonObject.add("certificate", new JsonPrimitive(b64Certificate));
            } catch (CertificateEncodingException e) {
                throw new IllegalStateException("Unable to encode certificate for InboundMetaData " + inboundMetadata.getTransmissionIdentifier());
            }

            return jsonObject;
        }
    }