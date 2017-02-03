package no.balder.spiralis;

import com.google.inject.Injector;
import eu.peppol.outbound.OxalisOutboundComponent;
import no.balder.spiralis.guice.JmsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.testng.Assert.assertEquals;


/**
 * @author steinar
 *         Date: 05.01.2017
 *         Time: 13.42
 */
public class GuiceTest {

    public static final Logger log = LoggerFactory.getLogger(GuiceTest.class);
    @Test
    public void testGuiceDataSource() throws Exception {

        OxalisOutboundComponent oxalisOutboundComponent = new OxalisOutboundComponent();
        Injector childInjector = oxalisOutboundComponent.getInjector().createChildInjector(
                new JmsModule("tcp://localhost:61616")
        );

        DataSource ds1 = childInjector.getInstance(DataSource.class);
        DataSource ds2 = childInjector.getInstance(DataSource.class);

        assertEquals(ds1, ds2);


    }
}
