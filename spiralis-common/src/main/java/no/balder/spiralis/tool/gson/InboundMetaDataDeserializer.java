package no.balder.spiralis.tool.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.vefa.peppol.common.model.*;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

class InboundMetaDataDeserializer implements JsonDeserializer<InboundMetadata> {

        final CertificateFactory certificateFactory;

        public InboundMetaDataDeserializer() {
            try {
                certificateFactory = CertificateFactory.getInstance("X.509");
            } catch (CertificateException e) {
                throw new IllegalStateException("Unable to create CerficateFactor: " + e.getMessage(), e);
            }
        }

        <T> T getJson(JsonElement jsonElement, JsonDeserializationContext ctx, String name, Class<T> type) {
            final JsonElement element = jsonElement.getAsJsonObject().get(name);
            return ctx.deserialize(element, type);
        }

        @Override
        public InboundMetadata deserialize(JsonElement j, Type type, JsonDeserializationContext ctx) throws JsonParseException {

            return new InboundMetadata() {
                @Override
                public X509Certificate getCertificate() {
                    final String asB64String = j.getAsJsonObject().get("certificate").getAsString();
                    final String pem = "-----BEGIN CERTIFICATE-----\n" + asB64String + "-----END CERTIFICATE-----\n";
                    try {
                        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(pem.getBytes()));
                    } catch (CertificateException e) {
                        throw new IllegalStateException("Unable to create certificate " + e.getMessage(), e);
                    }
                }

                @Override
                public TransmissionIdentifier getTransmissionIdentifier() {
                    return getJson(j, ctx, "messageId", TransmissionIdentifier.class);
                }

                @Override
                public Header getHeader() {
                    return getJson(j, ctx, "header", Header.class);
                }

                @Override
                public Date getTimestamp() {
                    return getJson(j, ctx, "timeStamp", Date.class);
                }

                @Override
                public Digest getDigest() {
                    return getJson(j, ctx, "digest", Digest.class);
                }

                @Override
                public TransportProtocol getTransportProtocol() {
                    return getJson(j, ctx, "transportProtocol", TransportProtocol.class);
                }

                @Override
                public TransportProfile getProtocol() {
                    return getJson(j, ctx, "transportProfile", TransportProfile.class);
                }

                @Override
                public List<Receipt> getReceipts() {
                    final JsonElement receipts = j.getAsJsonObject().get("receipts");
                    return ctx.deserialize(receipts, new TypeToken<List<Receipt>>() {
                    }.getType());
                }

                @Override
                public Receipt primaryReceipt() {
                    return getJson(j, ctx, "primaryReceipt", Receipt.class);
                }
            };
        }
    }
