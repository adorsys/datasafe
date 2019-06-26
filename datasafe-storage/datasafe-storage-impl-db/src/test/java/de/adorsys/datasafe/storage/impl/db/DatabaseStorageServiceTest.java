package de.adorsys.datasafe.storage.impl.db;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
class DatabaseStorageServiceTest extends BaseMockitoTest {

    private static DatabaseStorageService storageService;
    private static final URI uri = URI.create("jdbc://sa:sa@localhost:9999/h2/mem/test/private_profiles/path/hello.txt");
    private AbsoluteLocation<PrivateResource> location;

    @BeforeAll
    static void beforeAll() {
    }

    @SneakyThrows
    @BeforeEach
    public void beforeEach() {
        HashSet<String> allowedTables = new HashSet<>(Arrays.asList("users", "private_profiles", "public_profiles"));
        storageService = new DatabaseStorageService(allowedTables);

        location = BasePrivateResource.forAbsolutePrivate(uri);
        OutputStream write = storageService.write(location);
        write.write("HELLO".getBytes());
        write.close();
    }

    @AfterEach
    void afterEach() {
        String sql = "DROP ALL OBJECTS DELETE FILES";
        storageService.getJdbcTemplate().execute(sql);
    }

    @SneakyThrows
    @Test
    void objectExists() {
        location = BasePrivateResource.forAbsolutePrivate(uri);
        boolean exists = storageService.objectExists(location);
        Assert.assertTrue(exists);
    }

//    @SneakyThrows
//    @Test
//    void list() {
//        Stream<AbsoluteLocation<ResolvedResource>> list = storageService.list(location);
//        Assert.assertEquals(1, list.collect(Collectors.toList()).size());
//    }

    @SneakyThrows
    @Test
    void read() {
        InputStream read = storageService.read(location);
        String theString = IOUtils.toString(read);
        Assert.assertEquals(theString, "HELLO");
    }

    @SneakyThrows
    @Test
    void remove() {
        storageService.remove(location);
        Assertions.assertThrows(RuntimeException.class, () -> storageService.read(location));
    }

    @SneakyThrows
    @Test
    void write() {
        InputStream read = storageService.read(location);
        String theString = IOUtils.toString(read);
        Assert.assertEquals(theString, "HELLO");
    }
}
