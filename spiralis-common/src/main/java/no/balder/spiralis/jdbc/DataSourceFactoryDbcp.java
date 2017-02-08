package no.balder.spiralis.jdbc;

import javax.sql.DataSource;

/**
 * @author steinar
 *         Date: 07.02.2017
 *         Time: 17.11
 */
public interface DataSourceFactoryDbcp {
    DataSource getDataSource();
}
