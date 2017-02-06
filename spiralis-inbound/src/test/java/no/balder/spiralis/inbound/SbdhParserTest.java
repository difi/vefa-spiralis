package no.balder.spiralis.inbound;

import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.sbdh.SbdhReader;
import no.difi.vefa.peppol.sbdh.util.XMLStreamUtils;
import org.testng.annotations.Test;

import javax.xml.stream.XMLStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 19.53
 */
public class SbdhParserTest {

    @Test
    public void testParseSbdh() throws Exception {

        final URL resource = SbdhParserTest.class.getClassLoader().getResource("sample-with-sbdh-doc.xml");
        assertNotNull(resource);

        final SbdReader sbdReader = SbdReader.newInstance(Files.newInputStream(Paths.get(resource.toURI())));

        // Grabs the header
        final Header header = sbdReader.getHeader();
        System.out.println(header);
        assertNotNull(sbdReader);

        // Obtains a reader, which will read the embedded xml document
        final XMLStreamReader xmlStreamReader = sbdReader.xmlReader();

        XMLStreamUtils.copy(xmlStreamReader, System.out);

    }
}
