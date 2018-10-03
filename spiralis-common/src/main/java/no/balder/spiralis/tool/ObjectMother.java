package no.balder.spiralis.tool;

import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.tag.Tag;
import no.difi.vefa.peppol.common.code.DigestMethod;
import no.difi.vefa.peppol.common.model.*;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author steinar
 *         Date: 25.02.2017
 *         Time: 09.15
 */
public class ObjectMother {

    public static X509Certificate sampleCertificate() {
        // Old expired certificate only to be used for testing.
        String certificatePem = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEZDCCA0ygAwIBAgIQK9ylu/XV8IZAU1Ax82v+7DANBgkqhkiG9w0BAQsFADB9\n" +
                "MQswCQYDVQQGEwJESzEnMCUGA1UEChMeTkFUSU9OQUwgSVQgQU5EIFRFTEVDT00g\n" +
                "QUdFTkNZMR8wHQYDVQQLExZGT1IgVEVTVCBQVVJQT1NFUyBPTkxZMSQwIgYDVQQD\n" +
                "ExtQRVBQT0wgQUNDRVNTIFBPSU5UIFRFU1QgQ0EwHhcNMTUwNDIzMDAwMDAwWhcN\n" +
                "MTcwNDIyMjM1OTU5WjA2MQswCQYDVQQGEwJOTzEOMAwGA1UECgwFVW5pdDQxFzAV\n" +
                "BgNVBAMMDkFQUF8xMDAwMDAwMTExMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n" +
                "CgKCAQEAmrVOGKAuetZMrIHxMN+SB7ML6p4R5jNrNfMCzjQ5xC2f2/RmknzfSp4w\n" +
                "YwSwkamzY6WxOJuGjVnFF5APygvooEtwHzoXGXwhQwXYMzrmBfTuO5y8irD7WfP6\n" +
                "FhH+iU/VBeq6qm28uvQTkvm+K2lA4++PK1Nj1cYOwgrlyHbj4ENi7Z+r2iGTNiaX\n" +
                "c9pfQF6cLM7cDmdVrg7MvrdI3wJqeixTukG7YDPm5RFwtMGfAvnGO96tQQFG2ISF\n" +
                "MRSM1PsKBlWVvodRxKjEiWj54iRnjGFX5nGVL2uqpYtOPFY1X/H9LqU84vcdKfMR\n" +
                "9At4Fu3lC31dgGdyShDO1PmJ4c9HRQIDAQABo4IBJTCCASEwCQYDVR0TBAIwADAL\n" +
                "BgNVHQ8EBAMCA7gwdgYDVR0fBG8wbTBroGmgZ4ZlaHR0cDovL3BpbG90b25zaXRl\n" +
                "Y3JsLnZlcmlzaWduLmNvbS9EaWdpdGFsaXNlcmluZ3NzdHlyZWxzZW5QaWxvdE9w\n" +
                "ZW5QRVBQT0xBQ0NFU1NQT0lOVENBL0xhdGVzdENSTC5jcmwwHwYDVR0jBBgwFoAU\n" +
                "95aLGUyu4lYhAL6G/XpHrJ3KBXUwHQYDVR0OBBYEFGNpQv8WXvUpuCE1PznHYMzg\n" +
                "QvWIMDoGCCsGAQUFBwEBBC4wLDAqBggrBgEFBQcwAYYeaHR0cDovL3BpbG90LW9j\n" +
                "c3AudmVyaXNpZ24uY29tMBMGA1UdJQQMMAoGCCsGAQUFBwMCMA0GCSqGSIb3DQEB\n" +
                "CwUAA4IBAQBu7uzioz3A8duXo7mO1bNH/rcMHr5KJ5pqZ5F1Fxskr8NetSHm1uR3\n" +
                "/SK5WDNZud5ucZE4ViqYNWf5u0+WfMi2PmRygKmXQt0fyyBiTxdAWKtKsM/zq8TU\n" +
                "s/y8hf9G3/unXeGNKQ0xDUcv61drzEhlQtKDdtdk+cy1t/CCo2gbwgTNuTxgIP+B\n" +
                "a6k2RyRahbfz0Y367ChuqmUaCftaEbTvnLP0yg48PVNjwXVOI05msMUgEFr+GJ4n\n" +
                "/e6bU2cUwone7I/IKZNMslNMG5+6TMaCsS0E+207S4jff9t49kYRDy37rM9TXqBE\n" +
                "dyepjsmBJydKCDrfSKrzFyijUFYFcttn\n" +
                "-----END CERTIFICATE-----\n";

        final X509Certificate certificate;
        try {
            final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificatePem.getBytes()));
        } catch (CertificateException e) {
            throw new IllegalStateException("Unable to create X509 Certificate");
        }

        return certificate;
    }

    public static InboundMetadata createSampleInboundMetadata() {
        return new InboundMetadata() {
            TransmissionIdentifier transmissionIdentifier = TransmissionIdentifier.generateUUID();
            private Date creationTimestamp = new Date();
            private InstanceIdentifier instanceIdentifier = InstanceIdentifier.generateUUID();
            private Date date = new Date();

            @Override
            public TransmissionIdentifier getTransmissionIdentifier() {
                return transmissionIdentifier;
            }

            @Override
            public Header getHeader() {
                return Header.of(ParticipantIdentifier.of("9908:976098897"), ParticipantIdentifier.of("9908:123456789"), ProcessIdentifier.of("rubbish"), DocumentTypeIdentifier.of("rubbish"),
                        instanceIdentifier, InstanceType.of("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2", "Invoice", "2.0"), creationTimestamp);
            }

            @Override
            public Date getTimestamp() {
                return date;
            }

            @Override
            public Digest getDigest() {
                return Digest.of(DigestMethod.SHA1, "rubbish".getBytes());
            }

            @Override
            public TransportProtocol getTransportProtocol() {
                return TransportProtocol.AS2;
            }

            @Override
            public TransportProfile getProtocol() {
                return TransportProfile.AS2_1_0;
            }

            @Override
            public List<Receipt> getReceipts() {
                List<Receipt> receipts = new ArrayList<>();
                receipts.add(Receipt.of("Receipt", "receipt bytes".getBytes()));
                return receipts;
            }

            @Override
            public Receipt primaryReceipt() {
                return Receipt.of("Primary receipt", "Rubbish".getBytes());
            }

            @Override
            public X509Certificate getCertificate() {
                return ObjectMother.sampleCertificate();
            }

			@Override
			public Tag getTag() {
				// FIXME
				return Tag.of("SomeTag");
			}
        };
    }

}