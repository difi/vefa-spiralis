package no.balder.spiralis.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.outbound.OxalisOutboundComponent;
import eu.peppol.outbound.transmission.Transmitter;

/**
 * @author steinar
 *         Date: 05.12.2016
 *         Time: 17.01
 */
public class OxalisTransmissionModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(OxalisOutboundComponent.class).in(Singleton.class);
    }

    @Provides
    Transmitter provideTransmitter(OxalisOutboundComponent oxalisOutboundComponent) {
        Transmitter evidencePersistingTransmitter = oxalisOutboundComponent.getEvidencePersistingTransmitter();
        return evidencePersistingTransmitter;
    }


}

