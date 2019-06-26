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
import java.util.Arrays;
import java.util.HashSet;

@Slf4j
class DatabaseStorageServiceTest extends BaseMockitoTest {

    private static DatabaseStorageService storageService;

    @BeforeAll
    static void beforeAll() {
    }

    @BeforeEach
    public void BeforeEach() {
        storageService = new DatabaseStorageService(new HashSet<>(Arrays.asList("users", "private_profiles", "public_profiles")));
    }

    @SneakyThrows
    @Test
    void objectExists() {
        URI path = new URI("jdbc://sa:sa@localhost:9999/h2/mem/test/private_profiles");
        boolean exists = storageService.objectExists(BasePrivateResource.forAbsolutePrivate(path));
    }

    @SneakyThrows
    @Test
    void list() {
        URI path = new URI("jdbc://sa:sa@localhost:9999/h2/mem/test/private_profiles/path");
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
        URI path = new URI("jdbc://sa:sa@localhost:9999/h2/mem/test/private_profiles/path/hello.txt");
        OutputStream write = storageService.write(BasePrivateResource.forAbsolutePrivate(path));
        write.write("HELLO".getBytes());
        write.close();

        InputStream read = storageService.read(BasePrivateResource.forAbsolutePrivate(path));
        String theString = IOUtils.toString(read);
        Assert.assertEquals(theString, "HELLO");

    }
}
