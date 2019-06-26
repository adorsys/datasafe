package de.adorsys.datasafe.storage.impl.db;

import com.google.common.collect.ImmutableSet;
import de.adorsys.datasafe.types.api.resource.*;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThat;

@Slf4j
class DatabaseStorageServiceTest extends BaseMockitoTest {

    private static final AbsoluteLocation<PrivateResource> FILE = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate(
                    new Uri("jdbc://sa:sa@localhost:9999/h2/mem/test/private_profiles/path/hello.txt")
            )
    );

    private static final AbsoluteLocation<PrivateResource> OTHER_FILE = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate(
                    new Uri("jdbc://sa:sa@localhost:9999/h2/mem/test/private_profiles/path/hello1.txt")
            )
    );

    private static final String MESSAGE = "Hello!";

    private static final Set<String> ALLOWED_TABLES = ImmutableSet.of("users", "private_profiles", "public_profiles");

    private DatabaseStorageService storageService;
    private DatabaseConnectionRegistry connectionRegistry;

    @SneakyThrows
    @BeforeEach
    void beforeEach() {
        connectionRegistry = new DatabaseConnectionRegistry();
        storageService = new DatabaseStorageService(ALLOWED_TABLES, connectionRegistry);

        writeData(FILE, MESSAGE);
    }

    @AfterEach
    void afterEach() {
        connectionRegistry
                .jdbcTemplate(FILE)
                .execute("DROP ALL OBJECTS DELETE FILES");
    }

    @SneakyThrows
    @Test
    void objectExists() {
        assertThat(storageService.objectExists(FILE)).isTrue();
    }

    @SneakyThrows
    @Test
    void list() {
        AbsoluteLocation<PrivateResource> dir = new AbsoluteLocation<>(
                BasePrivateResource.forPrivate(
                        new Uri("jdbc://sa:sa@localhost:9999/h2/mem/test/private_profiles/path/")
                )
        );
        Stream<AbsoluteLocation<ResolvedResource>> list = storageService.list(dir);
        assertThat(list.collect(Collectors.toList())).hasSize(1);
    }

    @SneakyThrows
    @Test
    void read() {
        assertThat(storageService.read(FILE)).hasContent(MESSAGE);
    }

    @SneakyThrows
    @Test
    void remove() {
        storageService.remove(FILE);
        assertThat(storageService.objectExists(FILE)).isFalse();
    }

    @SneakyThrows
    @Test
    void write() {
        writeData(OTHER_FILE, MESSAGE);
        assertThat(storageService.read(OTHER_FILE)).hasContent(MESSAGE);
    }

    @SneakyThrows
    private void writeData(AbsoluteLocation path, String message) {
        try (OutputStream os = storageService.write(WithCallback.noCallback(path))) {
            os.write(message.getBytes());
        }
    }
}
