package no.balder.spiralis.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import no.balder.spiralis.inbound.SpiralisTask;
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
        bind(SpiralisTaskPersister.class).to(SpiralisTaskPersisterImpl.class);
    }


    @Provides
    @Singleton
    DataSource provideDefaultDataSource(DataSourceFactoryDbcp dataSourceFactoryDbcp) {
        LOGGER.warn("If you see this message more than once in production, data source is not being loaded in singleton");
        return dataSourceFactoryDbcp.getDataSource();
    }

    @Provides
    @Named("inMemory")
    @Singleton
    DataSource provideDataSourceInMemory() {
        JdbcDataSource ds = createH2DataSource();
        createDatabaseSchema(ds);
        return ds;
    }

    private JdbcDataSource createH2DataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    private static void createDatabaseSchema(DataSource ds) {
        try (InputStream resourceAsStream = DataSourceModule.class.getClassLoader().getResourceAsStream(CREATE_RINGO_DBMS_H2_SQL);){
            RunScript.execute(ds.getConnection(), new InputStreamReader(resourceAsStream, Charset.forName("UTF-8")));
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to obtain connection from datasource. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load SQL script from " + CREATE_RINGO_DBMS_H2_SQL + " : " + e.getMessage(), e);
        }
    }

}
