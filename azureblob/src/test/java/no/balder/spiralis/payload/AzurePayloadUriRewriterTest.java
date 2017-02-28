package no.balder.spiralis.payload;

import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 20.02.2017
 *         Time: 09.09
 */
public class AzurePayloadUriRewriterTest {

    @Test
    public void testRewrite() throws Exception {

        final AzurePayloadUriRewriter azurePayloadUriRewriter = new AzurePayloadUriRewriter();

        final URI uri = new URI("http://hmaptestdata01.blob.core.windows.net/invoice-out/sample-invoice-doc.xml");
        final URI rewritten = azurePayloadUriRewriter.rewrite(uri, null);

        System.out.println(rewritten);
    }

}