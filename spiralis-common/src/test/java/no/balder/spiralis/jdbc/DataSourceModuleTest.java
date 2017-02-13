package no.balder.spiralis.jdbc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.balder.spiralis.config.SpiralisConfigProperty;
import no.balder.spiralis.config.SpiralisConfigurationModule;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import static no.balder.spiralis.jdbc.JdbcConfiguration.*;
import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 16.53
 */
public class DataSourceModuleTest {


    /**
     * Verifies that we can configure the DataSource correctly
     * @throws Exception
     */
    @Test
    public void testJdbcConfig() throws Exception {

        // Overrides all parameters in any external .conf-files
        final Config config = createOverrideConf();

        final Injector i2 = Guice.createInjector(new SpiralisConfigurationModule(config),
                new DataSourceModule());

        final DataSourceFactoryDbcp dataSourceFactoryDbcp = i2.getInstance(DataSourceFactoryDbcp.class);

        // This only works on Steinars machine
        if ("steinar".equals(System.getProperty("user.name"))) {
            // Obtains the default DataSource
            final DataSource dataSource = i2.getInstance(DataSource.class);
            assertNotNull(dataSource);
            final String url = dataSource.getConnection().getMetaData().getURL();
            assertFalse(url.contains(":mem:"));
        }
    }

    @Test
    public void graphGuice() throws Exception {
        final Injector i2 = Guice.createInjector(new SpiralisConfigurationModule(createOverrideConf()),
                new DataSourceModule());

    }

    private Config createOverrideConf() {
        StringBuilder sb = new StringBuilder();
        sb.append(JDBC_CONNECTION_URI).append(" = ").append("\"jdbc:h2:~/.oxalis/ap;AUTO_SERVER=TRUE\"").append('\n');
        sb.append(JDBC_DRIVER_CLASS).append(" = ").append("\"org.h2.Driver\"").append('\n');

        // Driver is lazy loaded, so this is ok.
        sb.append(JDBC_DRIVER_CLASS_PATH).append(" = ").append("\"file:///Users/steinar/.m2/repository/com/h2database/h2/1.4.192/h2-1.4.192.jar\"").append('\n');
        sb.append(JDBC_USER).append(" = ").append("SA").append('\n');
        sb.append(JDBC_PASSWORD).append(" = ").append("\"\"").append('\n');

        return ConfigFactory.parseString(sb.toString());
    }

    /**
     * If we explicitly specify Named("inMemory"), we should get H2 in memory database
     * @throws Exception
     */
    @Test
    public void jdbcDataSourceWithDatabaseInMemory() throws Exception {

        final Injector injector = Guice.createInjector(new SpiralisConfigurationModule(ConfigFactory.empty()),
                new InMemoryDataSourceModule());

        final DataSource dsInMemory = injector.getInstance(DataSource.class);
        assertNotNull(dsInMemory);
        assertEquals(dsInMemory.getConnection().getMetaData().getURL(),"jdbc:h2:mem:test");
    }


    /**
     * Verifies that if we specify nothing, the H2 In memory database is used
     * @throws Exception
     */
    @Test
    public void defaultDsInMemory() throws Exception {

        // In case there is a ~/.spiralis/spiralis.conf, this will prevent it from being loaded.
        System.setProperty(SpiralisConfigProperty.SPIRALIS_HOME, System.getProperty("java.io.tmpdir"));
        final Injector injector = Guice.createInjector(new SpiralisConfigurationModule(ConfigFactory.empty()),
                new DataSourceModule());

        final DataSource ds = injector.getInstance(DataSource.class);
        final String url = ds.getConnection().getMetaData().getURL();
        assertEquals(url, "jdbc:h2:mem:test");
    }

    @Test
    public void connectToLocalDatabase() throws Exception {

        if ("steinar".equals(System.getProperty("user.name"))) {
            final Injector injector = Guice.createInjector(new SpiralisConfigurationModule(ConfigFactory.empty()), new DataSourceModule());
            final DataSource dataSource = injector.getInstance(DataSource.class);
            assertTrue(dataSource.getConnection().getMetaData().getURL().contains(":mem:") == false);

            final ResultSet tables = dataSource.getConnection().getMetaData().getTables(null, null, null, null);
            assertNotNull(tables);
            final ResultSetMetaData metaData = tables.getMetaData();
            while (tables.next()) {
                for (int i=1; i <= metaData.getColumnCount(); i++) {
                    // System.out.print(tables.getString(i) + " ");
                }
                // System.out.println();
            }
        }

    }
}