package no.balder.spiralis.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 13.02.2017
 *         Time: 12.58
 */
@Guice(moduleFactory = SpiralisInboundTestModuleFactory.class)
public class InboundModuleTest {

    @Inject
    Config config;

    @Test
    public void testVerifyInboundGlob() throws Exception {

        assertNotNull(config);

        assertTrue(config.hasPath(SpiralisConfigProperty.SPIRALIS_INBOUND_GLOB));

    }
}