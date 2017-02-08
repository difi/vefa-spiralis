package no.balder.spiralis.config;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.typesafe.config.ConfigFactory;
import no.balder.spiralis.jdbc.DataSourceModule;
import org.testng.IModuleFactory;
import org.testng.ITestContext;

/**
 * @author steinar
 *         Date: 05.02.2017
 *         Time: 20.27
 */
public class SpiralisInboundTestModuleFactory implements IModuleFactory {
    @Override
    public Module createModule(ITestContext iTestContext, Class<?> aClass) {
        return new TestModule();
    }

    static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            binder().install(new SpiralisConfigurationModule(ConfigFactory.empty()));
            binder().install(new DataSourceModule());
        }
    }
}

