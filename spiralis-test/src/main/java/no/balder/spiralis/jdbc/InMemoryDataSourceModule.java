package no.balder.spiralis.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
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
public class InMemoryDataSourceModule extends AbstractModule{

    public static final Logger LOGGER = LoggerFactory.getLogger(InMemoryDataSourceModule.class);

    private static final String CREATE_RINGO_DBMS_H2_SQL = "sql/create-dbms-h2.sql";

    @Override
    protected void configure() {
    }


    @Provides
    @Singleton
    DataSource provideDataSourceInMemory() {
        JdbcDataSource ds = createH2DataSource();
        createDatabaseSchema(ds);
        return ds;
    }

    private JdbcDataSource createH2DataSource() {
        LOGGER.info("Creating in memory DataSource ....");
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    private static void createDatabaseSchema(DataSource ds) {
        try (InputStream resourceAsStream = InMemoryDataSourceModule.class.getClassLoader().getResourceAsStream(CREATE_RINGO_DBMS_H2_SQL)){
            RunScript.execute(ds.getConnection(), new InputStreamReader(resourceAsStream, Charset.forName("UTF-8")));
            LOGGER.info("Created in memory database with complete schema ....");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to obtain connection from datasource. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load SQL script from " + CREATE_RINGO_DBMS_H2_SQL + " : " + e.getMessage(), e);
        }
    }

}
