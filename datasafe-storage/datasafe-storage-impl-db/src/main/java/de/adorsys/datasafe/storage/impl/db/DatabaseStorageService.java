package de.adorsys.datasafe.storage.impl.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.resource.*;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;

/*
//      input location format  jdbc://user:pass@host:port/database/table/key
 */
@Slf4j
public class DatabaseStorageService extends JdbcDaoSupport implements StorageService {

    private Set<String> allowedTables;

    public DatabaseStorageService(Set<String> allowedTables) {
        this.allowedTables = allowedTables;
    }

    @SneakyThrows
    public DatabaseStorageService(DataSource dataSource, Set<String> allowedTables) {
        setDataSource(dataSource);
        this.allowedTables = allowedTables;
        updateDbSchema(dataSource);
    }

    @Override
    public boolean objectExists(AbsoluteLocation location) {
        return false;
    }

    @SneakyThrows
    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        acquireConnectionToDbIfNeeded(location);
        String tableName = extractTable(location);
        String key = location.location().getPath();
        final String sql = "SELECT value FROM " + tableName + " WHERE key LIKE '?%'";
        List<String> keys = getJdbcTemplate().queryForList(sql, String.class, key);
        return keys.stream().map(it -> new AbsoluteLocation<>(
                new BaseResolvedResource(
                        new BasePrivateResource(new Uri(it)),
                        Instant.ofEpochMilli(currentTimeMillis()))
        ));
    }

    @SneakyThrows
    @Override
    public InputStream read(AbsoluteLocation location) {
        acquireConnectionToDbIfNeeded(location);
        String tableName = extractTable(location);
        String key = location.location().getPath();
        final String sql = "SELECT value FROM " + tableName + " where key = ?";
        ResultSetInputStream resultSetInputStream = new ResultSetInputStream(
                new ResultSetTpByteArrayIml(), getDataSource().getConnection(), sql, key);

        return resultSetInputStream;
    }

    @SneakyThrows
    @Override
    public void remove(AbsoluteLocation location) {
        acquireConnectionToDbIfNeeded(location);
        String tableName = extractTable(location);
        final String sql = "DELETE FROM " + tableName + " WHERE key = ?";
        log.debug("deleting: " + location.getResource().location());
        getJdbcTemplate().update(sql, location.location());
    }

    @SneakyThrows
    @Override
    public OutputStream write(AbsoluteLocation location) {
        acquireConnectionToDbIfNeeded(location);
        String tableName = extractTable(location);
        String key = location.location().getPath();
        // check table
        final String sql = "INSERT INTO " + tableName + " (id, user_id, key, value) VALUES(nextval('sq_private_profiles_id'), 1, ?, ?)";
        return new JdbcOutputStream(getJdbcTemplate(), sql, key);
    }

    private String extractTable(AbsoluteLocation location) {
        URI uri = location.location().asURI();
        if (uri.getPath() == null) {
            throw new RuntimeException("Wrong url format");
        }
        String[] uriParts = uri.getPath().split("/");
        if (!allowedTables.contains(uriParts[4])) {
            throw new RuntimeException("Wrong db table name");
        }
        return uriParts[4];
    }

    private void updateDbSchema(DataSource dataSource) throws SQLException, LiquibaseException {
        JdbcConnection connection = new JdbcConnection(dataSource.getConnection());
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        Liquibase liquibase = new Liquibase("changelog/changelog.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
    }

    private void acquireConnectionToDbIfNeeded(AbsoluteLocation location) throws SQLException, LiquibaseException {
        if (getDataSource() == null) {
            URI uri = location.location().asURI();
            if (uri.getUserInfo() == null || uri.getPath() == null) {
                throw new RuntimeException("Wrong url format");
            }
            String[] userInfo = uri.getUserInfo().split(":");
            String user = userInfo[0];
            String password = userInfo[1];
            String[] uriParts = uri.getPath().split("/");
            String url = uri.getScheme() + ":" + uriParts[1] + ":" + uriParts[2] + ":" + uriParts[3];
            HikariDataSource dataSource = getHikariDataSource(url, user, password);
            setDataSource(dataSource);
            updateDbSchema(dataSource);
        }
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
