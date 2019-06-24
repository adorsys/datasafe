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
import java.io.*;
import java.net.URI;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
/*
//      input location format  jdbc://user:pass@host:port/database/table/key
 */
@Slf4j
public class DatabaseStorageService extends JdbcDaoSupport implements StorageService {

    public DatabaseStorageService() {
    }

    @SneakyThrows
    public DatabaseStorageService(DataSource dataSource) {
        setDataSource(dataSource);
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
        // TODO query builder
        final String sql = "SELECT value FROM " + tableName + " WHERE key LIKE '" + key + "%'";
        List<String> keys = getJdbcTemplate().queryForList(sql, String.class);
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
        String value = getJdbcTemplate().queryForObject(sql, String.class, key);
        return new ByteArrayInputStream(value.getBytes());
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
        final String sql = "UPSERT INTO " + tableName + " (key, value) VALUES(?, ?)";
//        getJdbcTemplate().update(sql, )
        return new ByteArrayOutputStream();
    }

//    @Slf4j
//    @RequiredArgsConstructor
//    private static final class PutBlobOnClose extends ByteArrayOutputStream {
//
//        private final String bucketName;
//        private final ResourceLocation resource;
//
//        @Override
//        public void close() throws IOException {
//
//            ObjectMetadata metadata = new ObjectMetadata();
//            byte[] data = super.toByteArray();
//            metadata.setContentLength(data.length);
//
//            InputStream is = new ByteArrayInputStream(data);
//
//            String key = resource.location().getPath().replaceFirst("^/", "");
//            log.debug("Write to {}", Log.secure(key));
//            s3.putObject(bucketName, key, is, metadata);
//
//            super.close();
//        }
//    }

    private String extractDb(AbsoluteLocation location) {
//        "jdbc:h2"
//        "jdbc:postgresql:"
//        "jdbc:mysql
        String host = location.location().getWrapped().getHost();
        return DatabaseFactory.getInstance().findDefaultDriver(host);
    }

    private String extractTable(AbsoluteLocation location) {
        String[] splitted = location.getResource().location().getPath().split("/");
        return splitted[2];
    }

    private static HikariDataSource getHikariDataSource(String className, String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(className);
        config.setConnectionTestQuery("VALUES 1");
        config.addDataSourceProperty("URL", url);
        config.addDataSourceProperty("user", user);
        config.addDataSourceProperty("password", password);
        return new HikariDataSource(config);
    }

    private void updateDbSchema(DataSource dataSource) throws SQLException, LiquibaseException {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
        Liquibase liquibase = new Liquibase("changelog.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
    }

    private void acquireConnectionToDbIfNeeded(AbsoluteLocation location) throws SQLException, LiquibaseException {
        if (getDataSource() == null) {
            URI uri = location.location().asURI();
            String[] credsAndHost = uri.getAuthority().split("@");
            String creds = credsAndHost[0];
            String host = credsAndHost[1];
            String defaultDriver = extractDb(location);
            String[] userPass = creds.split(":");
            String user = userPass[0];
            String password = userPass[1];
            HikariDataSource dataSource = getHikariDataSource(defaultDriver, host, user, password);
            setDataSource(dataSource);
            updateDbSchema(dataSource);
        }
    }
}
