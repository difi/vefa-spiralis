package no.balder.spiralis.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import no.balder.spiralis.inbound.InboundDirector;
import no.balder.spiralis.jdbc.SpiralisTaskPersister;
import no.balder.spiralis.payload.AzurePayloadStore;
import no.balder.spiralis.payload.PayloadStore;

import javax.inject.Singleton;
import java.nio.file.Paths;

import static no.balder.spiralis.config.SpiralisConfigProperty.*;

/**
 * @author steinar
 *         Date: 13.02.2017
 *         Time: 11.37
 */
public class InboundModule extends AbstractModule{

    @Override
    protected void configure() {
        bind(PayloadStore.class).to(AzurePayloadStore.class);
    }


    @Provides
    @Singleton
    InboundDirector provideInboundDirector(Config config, PayloadStore payloadStore, SpiralisTaskPersister spiralisTaskPersister) {

        final String inboundDirname = config.getString(SPIRALIS_INBOUND_DIR);
        final String archiveDirName = config.getString(SPIRALIS_ARCHIVE_DIR);
        final String fileMatchPattern = config.getString(SPIRALIS_INBOUND_GLOB);

        return new InboundDirector(Paths.get(inboundDirname), Paths.get(archiveDirName), fileMatchPattern, payloadStore, spiralisTaskPersister);
    }

}

