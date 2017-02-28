package no.balder.spiralis.tool.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import no.balder.spiralis.tool.ObjectMother;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.commons.security.CertificateUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 21.02.2017
 *         Time: 08.02
 */
public class GsonHelperTest {

    @Test
    public void testPersist() throws Exception {

        final InboundMetadata inboundMetadata = ObjectMother.createSampleInboundMetadata();

        final String s = GsonHelper.toJson(inboundMetadata);

        assertNotNull(s);
        System.out.println(s);

        final InboundMetadata i2 = GsonHelper.fromJson(s);

        Assert.assertEquals(inboundMetadata.getTransmissionIdentifier(), i2.getTransmissionIdentifier());
        Assert.assertEquals(inboundMetadata.getHeader().getCreationTimestamp(), i2.getHeader().getCreationTimestamp());
        Assert.assertEquals(inboundMetadata.getHeader(), i2.getHeader());
        Assert.assertEquals(inboundMetadata.getTimestamp(), i2.getTimestamp());
        Assert.assertEquals(inboundMetadata.getDigest(), i2.getDigest());
        Assert.assertEquals(inboundMetadata.getTransportProtocol(), i2.getTransportProtocol());
        Assert.assertEquals(inboundMetadata.getProtocol(), i2.getProtocol());
        Assert.assertEquals(inboundMetadata.getReceipts(), i2.getReceipts());
        Assert.assertEquals(inboundMetadata.primaryReceipt(), i2.primaryReceipt());
        Assert.assertEquals(inboundMetadata.getCertificate(), i2.getCertificate());
    }


    @Test
    public void testWriteReadJson() throws Exception {
        final Path json = Files.createTempFile("json", ".json");
        final InboundMetadata sampleInboundMetadata;
        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(json)) {
            sampleInboundMetadata = ObjectMother.createSampleInboundMetadata();
            final String s = GsonHelper.toJson(sampleInboundMetadata);
            bufferedWriter.write(s);
        }

        final InboundMetadata inboundMetadata = GsonHelper.fromJson(json);
        assertEquals(inboundMetadata.getHeader(), sampleInboundMetadata.getHeader());
    }


    @Test
    public void testDate() throws Exception {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer());
        gsonBuilder.registerTypeAdapter(Date.class, new DateDeSerializer());
        final Gson gson = gsonBuilder.setPrettyPrinting().create();

        final Date src = new Date();
        final String s = gson.toJson(src, Date.class);
        final Date date = gson.fromJson(s, Date.class);
        assertEquals(src, date);
    }

    @Test
    public void grabApIdentifierFromCertificate() throws Exception {
        final X509Certificate x509Certificate = ObjectMother.sampleCertificate();
        System.out.println(x509Certificate.getSubjectDN().toString());
    }
}