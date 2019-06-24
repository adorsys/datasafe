package de.adorsys.datasafe.storage.impl.db;

import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

@Slf4j
class DatabaseStorageServiceTest extends BaseMockitoTest {

    private static final String H2_DB_URL = "jdbc:hsqldb:mem:testcase;shutdown=true";
    private static final String H2_DB_USER = "sa";
    private static final String H2_DB_PASSWORD = null;

    private static DatabaseStorageService storageService;

    @BeforeAll
    static void beforeAll() {
    }

    @BeforeEach
    public void BeforeEach() {
        storageService = new DatabaseStorageService();
    }

    @SneakyThrows
    @Test
    void objectExists() {
        URI path = new URI("jdbc://sa:sa@host/datasafedb/private_profiles/key");
        boolean exists = storageService.objectExists(BasePrivateResource.forAbsolutePrivate(path));
    }

    @SneakyThrows
    @Test
    void list() {
        URI path = new URI("jdbc://sa:sa@localhost/h2/private_profiles/deep/path/");
        storageService.list(BasePrivateResource.forAbsolutePrivate(path));
    }

    @SneakyThrows
    @Test
    void read() {
        //storageService.read(BasePrivateResource.forAbsolutePrivate())
    }

    @Test
    void remove() {
    }

    @SneakyThrows
    @Test
    void write() {
        URI path = new URI("jdbc://sa:sa@localhost/h2/private_profiles/deep/path/");
        OutputStream write = storageService.write(BasePrivateResource.forAbsolutePrivate(path));
        write.write("HELLO".getBytes());
        write.close();

        InputStream read = storageService.read(BasePrivateResource.forAbsolutePrivate(path));
        String theString = IOUtils.toString(read);
        Assert.assertEquals(theString, "HELLO");

    }

    private static DriverManagerDataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(H2_DB_URL);
        dataSource.setUsername(H2_DB_USER);
        dataSource.setPassword(H2_DB_PASSWORD);
        return dataSource;
    }
}
