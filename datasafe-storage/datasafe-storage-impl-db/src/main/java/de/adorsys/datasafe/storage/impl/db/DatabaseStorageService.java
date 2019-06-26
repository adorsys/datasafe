package de.adorsys.datasafe.storage.impl.db;

import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.*;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This storage adapter allows user to use relational database as storage.
 * input location format  jdbc://user:pass@host:port/database/table/user/path
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseStorageService implements StorageService {

    private final Set<String> allowedTables;
    private final DatabaseConnectionRegistry conn;

    @SneakyThrows
    @Override
    public boolean objectExists(AbsoluteLocation location) {
        String tableName = extractTable(location);
        String path = location.location().getPath();
        String pathWithUser = path.substring(path.indexOf(tableName) + tableName.length());
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE key = ?";
        return 0 != conn.jdbcTemplate(location).queryForObject(sql, Integer.class, pathWithUser);
    }

    @SneakyThrows
    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        String tableName = extractTable(location);
        String path = location.location().getPath();
        String pathWithUser = path.substring(path.indexOf(tableName) + tableName.length());

        String sql = "SELECT key,last_modified FROM " + tableName + " WHERE key LIKE '" + pathWithUser + "%'";

        List<Map<String, Object>> keys = conn.jdbcTemplate(location).queryForList(sql);
        return keys.stream().map(it -> new AbsoluteLocation<>(
                new BaseResolvedResource(
                        new BasePrivateResource(new Uri((String) it.get("key")).resolve(location.location())),
                        ((Date) it.get("last_modified")).toInstant()
                )
        ));
    }

    @SneakyThrows
    @Override
    public InputStream read(AbsoluteLocation location) {
        String tableName = extractTable(location);
        String path = location.location().getPath();
        String pathWithUser = path.substring(path.indexOf(tableName) + tableName.length());
        final String sql = "SELECT value FROM " + tableName + " WHERE key = ?";
        RowMapper<InputStream> rowMapper = (rs, i) -> rs.getClob("value").getAsciiStream();
        List<InputStream> values = conn.jdbcTemplate(location).query(sql, new Object[]{pathWithUser}, rowMapper);

        if (values.size() == 1) {
            return values.get(0);
        }
        throw new RuntimeException("No item found for id: " + pathWithUser);
    }

    @SneakyThrows
    @Override
    public void remove(AbsoluteLocation location) {
        String tableName = extractTable(location);
        String path = location.location().getPath();
        String pathWithUser = path.substring(path.indexOf(tableName) + tableName.length());
        final String sql = "DELETE FROM " + tableName + " WHERE key = ?";
        log.debug("deleting: " + pathWithUser);
        conn.jdbcTemplate(location).update(sql, pathWithUser);
    }

    @SneakyThrows
    @Override
    public OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback) {
        AbsoluteLocation location = locationWithCallback.getWrapped();
        String tableName = extractTable(location);
        String path = location.location().getPath();
        String pathWithUser = path.substring(path.indexOf(tableName) + tableName.length());
        return new PutBlobOnClose(conn.jdbcTemplate(location), pathWithUser, tableName);
    }


    @Slf4j
    @RequiredArgsConstructor
    private static final class PutBlobOnClose extends ByteArrayOutputStream {

        private final JdbcTemplate jdbcTemplate;
        private final String pathWithUser;
        private final String tableName;

        @Override
        public void close() throws IOException {
            final String sql = "INSERT INTO " + tableName + " (key, value) VALUES(?, ?)";
            KeyHolder holder = new GeneratedKeyHolder();
            jdbcTemplate.update(writeData(sql), holder);
            super.close();
        }

        private PreparedStatementCreator writeData(String sql) {
            return connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, pathWithUser);
                byte[] data = super.toByteArray();
                Reader reader = new InputStreamReader(new ByteArrayInputStream(data));
                ps.setClob(2, reader);
                return ps;
            };
        }
    }

    private String extractTable(AbsoluteLocation location) {
        URI uri = location.location().asURI();

        if (uri.getPath() == null) {
            throw new IllegalArgumentException("Wrong url format");
        }

        String[] uriParts = uri.getPath().split("/");

        if (!allowedTables.contains(uriParts[4])) {
            throw new IllegalArgumentException("Wrong db table name");
        }

        return uriParts[4];
    }
}
