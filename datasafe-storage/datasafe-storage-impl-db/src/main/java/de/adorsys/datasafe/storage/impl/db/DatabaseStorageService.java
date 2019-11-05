package de.adorsys.datasafe.storage.impl.db;

import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BaseResolvedResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
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
@RequiredArgsConstructor
public class DatabaseStorageService implements StorageService {

    /**
     * Filters tables that can be used in connection URI
     */
    private final Set<String> allowedTables;

    /**
     * Database connection cache
     */
    private final DatabaseConnectionRegistry conn;

    @SneakyThrows
    @Override
    public boolean objectExists(AbsoluteLocation location) {
        ParsedLocation parsed = new ParsedLocation(location, allowedTables);
        String sql = "SELECT COUNT(*) FROM " + parsed.getTableName() + " WHERE `key` = ?";
        return 0 != conn.jdbcTemplate(location).queryForObject(sql, Integer.class, parsed.getPathWithUser());
    }

    @SneakyThrows
    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        ParsedLocation parsed = new ParsedLocation(location, allowedTables);
        String sql = "SELECT `key`,`last_modified` FROM " + parsed.getTableName() + " WHERE `key` LIKE '"
                + parsed.getPathWithUser() + "%'";

        List<Map<String, Object>> keys = conn.jdbcTemplate(location).queryForList(sql);
        return keys.stream().map(it -> new AbsoluteLocation<>(
                new BaseResolvedResource(
                        createPath(location, (String) it.get("key")),
                        ((Date) it.get("last_modified")).toInstant()
                )
        ));
    }

    @SneakyThrows
    @Override
    public InputStream read(AbsoluteLocation location) {
        ParsedLocation parsed = new ParsedLocation(location, allowedTables);
        final String sql = "SELECT value FROM " + parsed.getTableName() + " WHERE `key` = ?";
        RowMapper<InputStream> rowMapper = (rs, i) -> rs.getClob("value").getAsciiStream();
        List<InputStream> values = conn.jdbcTemplate(location).query(
                sql,
                new Object[] {parsed.getPathWithUser()},
                rowMapper
        );

        if (values.size() == 1) {
            return values.get(0);
        }
        throw new IllegalArgumentException("No item found");
    }

    @SneakyThrows
    @Override
    public void remove(AbsoluteLocation location) {
        ParsedLocation parsed = new ParsedLocation(location, allowedTables);
        String sql = "DELETE FROM " + parsed.getTableName() + " WHERE `key` = ?";
        conn.jdbcTemplate(location).update(sql, parsed.getPathWithUser());
    }

    @SneakyThrows
    @Override
    public OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback) {
        ParsedLocation parsed = new ParsedLocation(locationWithCallback.getWrapped(), allowedTables);

        return new PutBlobOnClose(
                conn.jdbcTemplate(locationWithCallback.getWrapped()), parsed.getPathWithUser(), parsed.getTableName()
        );
    }

    private PrivateResource createPath(AbsoluteLocation root, String key) {
        String fullUri = root.location().withoutAuthority().toASCIIString();
        int keyIndex = fullUri.indexOf(key);

        AbsoluteLocation resourceRoot =
                BasePrivateResource.forAbsolutePrivate(new Uri(root.location().withoutAuthority()));

        if (keyIndex >= 0) {
            resourceRoot = BasePrivateResource.forAbsolutePrivate(new Uri(fullUri.substring(0, keyIndex)));
        }

        return BasePrivateResource
                .forPrivate(key)
                .resolveFrom(resourceRoot);
    }

    @Slf4j
    @RequiredArgsConstructor
    private static final class PutBlobOnClose extends ByteArrayOutputStream {

        private final JdbcTemplate jdbcTemplate;
        private final String pathWithUser;
        private final String tableName;

        @Override
        public void close() throws IOException {
            String sql = "INSERT INTO " + tableName + " (`key`, `value`) VALUES(?, ?)";
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

    @Getter
    private static final class ParsedLocation {

        private final Set<String> allowedTables;
        private final String tableName;
        private final String path;
        private final String pathWithUser;

        ParsedLocation(AbsoluteLocation location, Set<String> allowedTables) {
            this.allowedTables = allowedTables;
            this.tableName = extractTable(location);
            this.path = location.location().getRawPath();
            this.pathWithUser = path.substring(path.indexOf(tableName) + tableName.length() + 1);
        }

        private String extractTable(AbsoluteLocation location) {
            URI uri = location.location().asURI();

            if (uri.getPath() == null) {
                throw new IllegalArgumentException("Wrong url format");
            }
            //TODO add example of url
            String[] uriParts = uri.getPath().split("/");

            if (!allowedTables.contains(uriParts[2])) {
                throw new IllegalArgumentException("Wrong db table name");
            }

            return uriParts[2];
        }
    }
}
