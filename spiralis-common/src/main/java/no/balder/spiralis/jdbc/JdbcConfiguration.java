package no.balder.spiralis.jdbc;

import com.typesafe.config.Config;

import javax.inject.Inject;
import java.util.Optional;


/**
 * @author steinar
 *         Date: 30.01.2017
 *         Time: 08.55
 */
public class JdbcConfiguration {

    public static final String JDBC_CONNECTION_URI = "jdbc.connection.uri";
    public static final String JDBC_DRIVER_CLASS_PATH = "jdbc.driver.class.path";
    public static final String JDBC_DRIVER_CLASS = "jdbc.driver.class.name";
    public static final String JDBC_USER = "jdbc.user";
    public static final String JDBC_PASSWORD = "jdbc.password";
    public static final String JDBC_VALIDATION_QUERY = "jdbc.validation.query";


    String jdbcConnectionUri;

    Optional<String> jdbcDriverClassPath = Optional.empty();

    String jdbcDriverClassName;

    String jdbcUsername;

    String jdbcPassword;

    Optional<String> validationQuery = Optional.empty();


    @Inject
    public JdbcConfiguration(Config config) {

        this.jdbcConnectionUri = config.getString(JDBC_CONNECTION_URI);
        if (config.hasPath(JDBC_DRIVER_CLASS_PATH)) {
            final String cp = config.getString(JDBC_DRIVER_CLASS_PATH);
            if (cp.trim().length() > 0) {
                this.jdbcDriverClassPath = Optional.of(cp);
            }
        }
        this.jdbcDriverClassName = config.getString(JDBC_DRIVER_CLASS);
        this.jdbcUsername = config.getString(JDBC_USER);
        this.jdbcPassword = config.getString(JDBC_PASSWORD);
        if (config.hasPath(JDBC_VALIDATION_QUERY)) {

            final String validatQuery = config.getString(JDBC_VALIDATION_QUERY);
            if (validatQuery.trim().length() > 0) {
                this.validationQuery = Optional.of(validatQuery);
            }
        }
    }


    public String getJdbcConnectionUri() {
        return jdbcConnectionUri;
    }

    public Optional<String> getJdbcDriverClassPath() {
        return jdbcDriverClassPath;
    }

    public String getJdbcDriverClassName() {
        return jdbcDriverClassName;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public Optional<String> getValidationQuery() {
        return validationQuery;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JdbcConfiguration{");
        sb.append("jdbcConnectionUri='").append(jdbcConnectionUri).append('\'');
        sb.append(", jdbcDriverClassPath=").append(jdbcDriverClassPath);
        sb.append(", jdbcDriverClassName='").append(jdbcDriverClassName).append('\'');
        sb.append(", jdbcUsername='").append(jdbcUsername).append('\'');
        sb.append(", jdbcPassword='").append(jdbcPassword).append('\'');
        sb.append(", validationQuery=").append(validationQuery);
        sb.append('}');
        return sb.toString();
    }
}
