/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.balder.spiralis.jdbc;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Optional;
import java.util.Properties;

/**
 * Thread safe and singleton. I.e. will always return the same DataSource.
 * </p>
 *
 * @author steinar
 *         Date: 18.04.13
 *         Time: 13:28
 */
public class DataSourceFactoryDbcpImpl implements DataSourceFactoryDbcp {

    public static final Logger log = LoggerFactory.getLogger(DataSourceFactoryDbcpImpl.class);

    private final JdbcConfiguration configuration;

    private volatile DataSource dataSource;

    @Inject
    public DataSourceFactoryDbcpImpl(JdbcConfiguration configuration) {
        this.configuration = configuration;
        log.info("DataSource being connected with config " + configuration.toString());

    }

    @Override
    public DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    dataSource = configureAndCreateDataSource(configuration);
                }
            }
        }
        return dataSource;
    }

    /**
     * Creates a DataSource with connection pooling as provided by Apache DBCP
     *
     * @param configuration
     * @return a DataSource
     */
    DataSource configureAndCreateDataSource(JdbcConfiguration configuration) {

        log.debug("Configuring DataSource wrapped in a Database Connection Pool, using these properties: " + configuration);

        Optional<String> jdbcDriverClassPath = configuration.getJdbcDriverClassPath();

        log.debug("Loading JDBC Driver with custom class path: " + jdbcDriverClassPath);

        // Optionally creates a new class loader, which will be used for loading our JDBC driver
        ClassLoader classLoader = getOxalisClassLoaderForJdbc(jdbcDriverClassPath);

        String className = configuration.getJdbcDriverClassName();
        String connectURI = configuration.getJdbcConnectionUri().toString();
        String userName = configuration.getJdbcUsername();
        String password = configuration.getJdbcPassword();

        // Loads the JDBC Driver in an optional separate class loader
        Driver driver = getJdbcDriver(classLoader, className);


        // Creates the DBCP DataSource
        PoolingDataSource poolingDataSource = getPoolingDataSource(configuration, connectURI, userName, password, driver);

        return poolingDataSource;

    }

    private PoolingDataSource getPoolingDataSource(JdbcConfiguration configuration, String connectURI, String userName, String password, Driver driver) {
        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);

        // DBCP factory which will produce JDBC Driver instances
        ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, connectURI, properties);


        // DBCP Factory holding the pooled connection, which are created by the driver connection factory and held in the supplied pool
        ObjectName dataSourceJmxName;
        try {
            dataSourceJmxName = new ObjectName("no.difi.oxalis", "connectionPool", "OxalisDB");
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory, dataSourceJmxName);

        if (configuration.getValidationQuery().isPresent()) {
            poolableConnectionFactory.setValidationQuery(configuration.getValidationQuery().get());
        }
        // DBCP object pool holding our driver connections
        GenericObjectPool<PoolableConnection> genericObjectPool = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(genericObjectPool);
        genericObjectPool.setMaxTotal(100);
        genericObjectPool.setMaxIdle(30);
        genericObjectPool.setMaxWaitMillis(10000);

        genericObjectPool.setTestOnBorrow(true);    // Test the connection returned from the pool

        genericObjectPool.setTestWhileIdle(true);   // Test idle instances visited by the pool maintenance thread and destroy any that fail validation
        genericObjectPool.setTimeBetweenEvictionRunsMillis(60 * 60 * 1000);      // Test every hour

        // Creates the actual DataSource instance
        //noinspection unchecked
        return new PoolingDataSource(genericObjectPool);
    }

    /**
     * Creates a {@link URLClassLoader} if the supplied JDBC driver class path was supplied, otherwise uses the
     * default class loader.
     *
     * @param jdbcDriverClassPath
     * @return
     */
    private static ClassLoader getOxalisClassLoaderForJdbc(Optional<String> jdbcDriverClassPath) {
        URLClassLoader urlClassLoader;

        if (!jdbcDriverClassPath.isPresent()) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            try {
                urlClassLoader = new URLClassLoader(new URL[]{new URL(jdbcDriverClassPath.get())}, Thread.currentThread().getContextClassLoader());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid jdbc driver class path: '" + jdbcDriverClassPath, e);
            }
            return urlClassLoader;
        }
    }

    private static Driver getJdbcDriver(ClassLoader classLoader, String className) {
        Class<?> aClass;
        try {
            aClass = Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to locate class " + className + " using " + classLoader.toString());
        }
        Driver driver;
        try {
            driver = (Driver) aClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Unable to instantiate driver from class " + className, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access driver class " + className + "; " + e, e);
        }
        return driver;
    }
}
