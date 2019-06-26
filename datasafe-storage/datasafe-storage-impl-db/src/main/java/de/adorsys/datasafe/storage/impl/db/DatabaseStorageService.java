package de.adorsys.datasafe.storage.impl.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.resource.*;
import de.adorsys.datasafe.types.api.utils.Log;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.IOUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.io.*;
import java.net.URI;
import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;

/*
//      input location format  jdbc://user:pass@host:port/database/table/user/path
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
        String path = location.location().getPath();
        String pathWithUser = path.substring(path.indexOf(tableName) + tableName.length());
        final String sql = "SELECT value FROM " + tableName + " WHERE key LIKE '" + pathWithUser + "%'";
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
        String path = location.location().getPath();
        String pathWithUser = path.substring(path.indexOf(tableName) + tableName.length());
        final String sql = "SELECT value FROM " + tableName + " where key = ?";
        RowMapper<String> rowMapper = (rs, i) -> {
            InputStream contentStream = rs.getClob("value").getAsciiStream();
            return new Scanner(contentStream, "UTF-8").useDelimiter("\\A").next();
//            return rs.getBinaryStream("value");
        };
        List<String> values = getJdbcTemplate().query(sql, new Object[]{pathWithUser}, rowMapper);

        if (values.size() == 1) {
            return new ByteArrayInputStream(values.get(0).getBytes());
        }
        throw new RuntimeException("No item found for id: " + pathWithUser);
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
        String path = location.location().getPath();
        String pathWithUser = path.substring(path.indexOf(tableName) + tableName.length());
        return new PutBlobOnClose(getJdbcTemplate(), pathWithUser, tableName);
    }

    @Slf4j
    @RequiredArgsConstructor
    private static final class PutBlobOnClose extends ByteArrayOutputStream {

        private final JdbcTemplate jdbcTemplate;
        private final String pathWithUser;
        private final String tableName;

        @Override
        public void close() throws IOException {

            final String sql = "INSERT INTO " + tableName + " (id, key, value) VALUES(nextval('sq_private_profiles_id'), ?, ?)";
            KeyHolder holder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, pathWithUser);
                byte[] data = super.toByteArray();
                Reader reader = new InputStreamReader(new ByteArrayInputStream(data));
                ps.setClob(2, reader);
                return ps;
            }, holder);
            Number key = holder.getKey();
            log.debug("Write to db record with key {}", Log.secure(key));
            super.close();
        }
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
