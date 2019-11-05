package de.adorsys.datasafe.storage.impl.db;

import com.google.common.collect.ImmutableSet;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DatabaseStorageServiceTest extends BaseMockitoTest {

    private static final AbsoluteLocation<PrivateResource> ROOT = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate(
                    new Uri("jdbc://sa:sa@localhost:9999/h2:mem:test/private_profiles/")
            )
    );

    private static final AbsoluteLocation<PrivateResource> FILE = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate(
                    new Uri("jdbc://sa:sa@localhost:9999/h2:mem:test/private_profiles/path/hello.txt")
            )
    );

    private static final AbsoluteLocation<PrivateResource> OTHER_FILE = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate(
                    new Uri("jdbc://sa:sa@localhost:9999/h2:mem:test/private_profiles/path/hello1.txt")
            )
    );

    private static final String MESSAGE = "Hello!";

    private static final Set<String> ALLOWED_TABLES = ImmutableSet.of("users", "private_profiles", "public_profiles");

    private DatabaseStorageService storageService;
    private DatabaseConnectionRegistry connectionRegistry;

    @BeforeEach
    @SneakyThrows
    void init() {
        connectionRegistry = new DatabaseConnectionRegistry(
                uri -> uri.location().getWrapped().getScheme() + ":" + uri.location().getPath().split("/")[1],
                Collections.emptyMap()
        );
        storageService = new DatabaseStorageService(ALLOWED_TABLES, connectionRegistry);

        writeData(FILE, MESSAGE);
    }

    @AfterEach
    void afterEach() {
        connectionRegistry
                .jdbcTemplate(FILE)
                .execute("DROP ALL OBJECTS DELETE FILES");
    }

    @Test
    void objectExists() {
        assertThat(storageService.objectExists(FILE)).isTrue();
    }

    @Test
    void list() {
        writeData(OTHER_FILE, MESSAGE);

        try (Stream<AbsoluteLocation<ResolvedResource>> ls = storageService.list(FILE)) {
            assertThat(ls)
                    .extracting(it -> it.getResource().location().asString())
                    .containsOnly("jdbc://localhost:9999/h2:mem:test/private_profiles/path/hello.txt");
        }

        try (Stream<AbsoluteLocation<ResolvedResource>> ls = storageService.list(ROOT)) {
            assertThat(ls)
                    .extracting(it -> it.getResource().location().asString())
                    .containsOnly(
                            "jdbc://localhost:9999/h2:mem:test/private_profiles/path/hello.txt",
                            "jdbc://localhost:9999/h2:mem:test/private_profiles/path/hello1.txt"
                    );
        }
    }

    @Test
    @SneakyThrows
    void read() {
        try (InputStream read = storageService.read(FILE)) {
            assertThat(read).hasContent(MESSAGE);
        }
    }

    @Test
    void remove() {
        storageService.remove(FILE);
        assertThat(storageService.objectExists(FILE)).isFalse();
    }

    @Test
    @SneakyThrows
    void write() {
        writeData(OTHER_FILE, MESSAGE);
        try (InputStream read = storageService.read(OTHER_FILE)) {
            assertThat(read).hasContent(MESSAGE);
        }
    }

    @SneakyThrows
    private void writeData(AbsoluteLocation path, String message) {
        try (OutputStream os = storageService.write(WithCallback.noCallback(path))) {
            os.write(message.getBytes());
        }
    }
}
