package no.balder.spiralis.jdbc;

import com.google.inject.AbstractModule;

/**
 * Binds the various Repository classes.
 * 
 * @author steinar
 *         Date: 09.02.2017
 *         Time: 18.26
 */
public class RepositoryModule extends AbstractModule {


    @Override
    protected void configure() {
        bind(SpiralisTaskPersister.class).to(SpiralisTaskPersisterImpl.class);
    }
}
