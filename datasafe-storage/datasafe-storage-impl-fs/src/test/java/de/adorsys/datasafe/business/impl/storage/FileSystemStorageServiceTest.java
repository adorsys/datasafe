package de.adorsys.datasafe.business.impl.storage;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;

class FileSystemStorageServiceTest extends BaseMockitoTest {

    private static final String FILE = "file";
    private static final String MESSAGE = "hello";

    private FileSystemStorageService storageService;
    private AbsoluteResourceLocation<PrivateResource> root;
    private AbsoluteResourceLocation<PrivateResource> fileWithMsg;
    private Path storageDir;

    @BeforeEach
    void prepare(@TempDir Path dir) {
        this.storageService = new FileSystemStorageService(dir.toUri());
        this.storageDir = dir;
        this.root = new AbsoluteResourceLocation<>(DefaultPrivateResource.forPrivate(dir.toUri()));
        this.fileWithMsg = new AbsoluteResourceLocation<>(
                DefaultPrivateResource.forPrivate(storageDir.toUri().resolve(FILE))
        );
    }

    @Test
    void list() {
        createFileWithMessage();

        assertThat(storageService.list(root))
                .hasSize(1)
                .extracting(AbsoluteResourceLocation::location)
                .asString().contains(FILE);
    }

    @Test
    void listOnNonExisting() {
        assertThat(storageService.list(root)).isEmpty();
    }

    @Test
    void read() {
        createFileWithMessage();

        assertThat(storageService.read(fileWithMsg)).hasContent(MESSAGE);
    }

    @Test
    @SneakyThrows
    void write() {
        try (OutputStream os = storageService.write(fileWithMsg)) {
            os.write(MESSAGE.getBytes());
        }

        assertThat(storageService.read(fileWithMsg)).hasContent(MESSAGE);
    }

    @Test
    void remove() {
        createFileWithMessage();
        // precondition:
        assertThat(Paths.get(fileWithMsg.location())).exists();

        storageService.remove(fileWithMsg);

        assertThat(Paths.get(fileWithMsg.location())).doesNotExist();
    }

    @SneakyThrows
    private void createFileWithMessage() {
        Files.write(storageDir.resolve(FILE), MESSAGE.getBytes(), StandardOpenOption.CREATE);
    }
}