package no.balder.spiralis;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import eu.peppol.persistence.guice.RepositoryModule;
import eu.peppol.persistence.test.TestInMemoryDatabaseModule;
import no.balder.spiralis.guice.JmsModule;
import org.testng.IModuleFactory;
import org.testng.ITestContext;

/**
 * @author steinar
 *         Date: 01.12.2016
 *         Time: 11.55
 */
public class TestModuleFactory implements IModuleFactory {
    @Override
    public Module createModule(ITestContext iTestContext, Class<?> aClass) {
        return new InMemoryDatabaseModule();
    }

    class InMemoryDatabaseModule extends AbstractModule {

        @Override
        protected void configure() {
            binder().install(new RepositoryModule());
            binder().install(new TestInMemoryDatabaseModule());
            binder().install(new JmsModule());
        }
    }
}
