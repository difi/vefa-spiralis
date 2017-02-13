package no.balder.spiralis.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 16.51
 */
public class DataSourceModule extends AbstractModule{

    public static final Logger LOGGER = LoggerFactory.getLogger(DataSourceModule.class);

    private static final String CREATE_RINGO_DBMS_H2_SQL = "sql/create-dbms-h2.sql";

    @Override
    protected void configure() {

        bind(DataSourceFactoryDbcp.class).to(DataSourceFactoryDbcpImpl.class);
    }


    @Provides
    @Singleton
    DataSource provideDefaultDataSource(DataSourceFactoryDbcp dataSourceFactoryDbcp) {
        LOGGER.warn("If you see this message more than once in production, data source is not being loaded in singleton");
        return dataSourceFactoryDbcp.getDataSource();
    }
}
