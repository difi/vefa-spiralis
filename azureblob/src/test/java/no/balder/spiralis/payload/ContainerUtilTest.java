package no.balder.spiralis.payload;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 17.54
 */
public class ContainerUtilTest {


    @Test
    public void testFileNameExtraction() throws Exception {

        final Path path = Paths.get("/var/peppol/IN/" + "134f84fc-50b9-41d9-a77f-854518bbbccf-doc.xml");
        final String s = ContainerUtil.getFileNameOnly(path);
        assertEquals(s, "134f84fc-50b9-41d9-a77f-854518bbbccf-doc.xml");
    }



    @Test
    public void testContainerName() throws Exception {

        final String s = ContainerUtil.containerNameFor(Paths.get("/var/peppol/IN/9908_971589671/9908_848382922/2017-01-10/17524837-551a-4316-b3a3-feb9ebd84ac0-doc.xml"));
        assertNotNull(s);
        assertEquals(s, "peppol-ap-05");


        final String s1 = ContainerUtil.containerNameFor(Paths.get("/Users/steinar/src/spiralis/azureblob/target/test-classes/sample-invoice.xml"));
        assertEquals(s1, "peppol-ap-22");

    }

}