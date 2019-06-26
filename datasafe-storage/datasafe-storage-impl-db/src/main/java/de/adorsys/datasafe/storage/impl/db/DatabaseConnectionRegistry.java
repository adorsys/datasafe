package de.adorsys.datasafe.storage.impl.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class acts as higher-level DataSource cache.
 */
public class DatabaseConnectionRegistry {

    private final Map<String, JdbcDaoSupport> dataSourceCache;
    private final Map<String, DatabaseCredentials> providedCredentials;

    public DatabaseConnectionRegistry() {
        this.dataSourceCache = new ConcurrentHashMap<>();
        this.providedCredentials = Collections.emptyMap();
    }

    /**
     * Pre-populates registry with credentials associated with URI prefixes, so that one
     * can use this registry to obtain connections using URI's without user information.
     * @param providedCredentials Prefix-based matcher for db URI - connection credentials
     */
    public DatabaseConnectionRegistry(Map<String, DatabaseCredentials> providedCredentials) {
        this.dataSourceCache = new ConcurrentHashMap<>();
        this.providedCredentials = Collections.emptyMap();
    }

    /**
     * Acquire JDBC template for some resource location
     * @param location Resource location that has credentials to access database.
     * @return Jdbc template to use with this connection
     */
    public JdbcTemplate jdbcTemplate(AbsoluteLocation location) {
        return dataSourceCache
                .computeIfAbsent(connectionKey(location), key -> acquireDaoSupport(location, getCredentials(location)))
                .getJdbcTemplate();
    }

    /**
     * Migrates specified data source using liquibase script at changelog/changelog.xml
     * @param dataSource DataSource to migrate
     */
    @SneakyThrows
    protected void updateDbSchema(DataSource dataSource) {
        try (Connection dbConn = dataSource.getConnection()) {
            JdbcConnection connection = new JdbcConnection(dbConn);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            Liquibase liquibase = new Liquibase(
                    "changelog/changelog.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update(new Contexts(), new LabelExpression());
        }
    }

    /**
     * Open `database connection`
     * @param location Location with credentials to open connection for
     * @return JdbcDaoSupport object that has database migrated using liquibase.
     */
    protected JdbcDaoSupport acquireDaoSupport(AbsoluteLocation location, DatabaseCredentials credentials) {
        URI uri = location.location().asURI();

        if (uri.getPath() == null) {
            throw new IllegalArgumentException("Wrong url format");
        }


        String[] uriParts = uri.getPath().split("/");
        String url = uri.getScheme() + ":" + uriParts[1] + ":" + uriParts[2] + ":" + uriParts[3];

        JdbcDaoSupport daoSupport = new JdbcDaoSupport() {};
        DataSource dataSource = getHikariDataSource(url, credentials.getUsername(), credentials.getPassword());
        daoSupport.setDataSource(dataSource);
        updateDbSchema(dataSource);
        return daoSupport;
    }

    /**
     * Extracts credentials from path URI or uses {@code providedCredentials} to get them
     * @param location URI to get credentials for.
     * @return Credentials to access that URI.
     */
    protected DatabaseCredentials getCredentials(AbsoluteLocation location) {
        URI uri = location.location().asURI();
        String userInfo = uri.getUserInfo();

        if (null !=  userInfo && !"".equals(userInfo)) {
            return new DatabaseCredentials(location);
        }

        return providedCredentials.entrySet().stream()
                .filter(it -> uri.toASCIIString().startsWith(it.getKey()))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("There is no associated database for this credentials")
                ).getValue();
    }

    // includes credentials if they are present in URI
    private String connectionKey(AbsoluteLocation location) {
        URI target = location.location().asURI();
        return target.getScheme() + target.getHost();
    }

    private static HikariDataSource getHikariDataSource(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setConnectionTestQuery("VALUES 1");
        config.addDataSourceProperty("user", user);
        config.addDataSourceProperty("password", password);
        return new HikariDataSource(config);
    }
}
