package no.balder.spiralis.inbound;

import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 03.02.2017
 *         Time: 19.18
 */
public class InboundDirectorTest {

    @Test
    public void testStart() throws Exception {

        final InboundDirector inboundDirector = new InboundDirector(Paths.get("/var/peppol/IN"));

        inboundDirector.start();
    }

}