package de.adorsys.datasafe.storage.impl.fs;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.shared.BaseMockitoTest;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class FileSystemStorageServiceTest extends BaseMockitoTest {

    private static final String FILE = "file";
    private static final String MESSAGE = "hello";

    private FileSystemStorageService storageService;
    private AbsoluteLocation<PrivateResource> root;
    private AbsoluteLocation<PrivateResource> fileWithMsg;
    private Path storageDir;

    @BeforeEach
    void prepare(@TempDir Path dir) {
        this.storageService = new FileSystemStorageService(dir.toUri());
        this.storageDir = dir;
        this.root = new AbsoluteLocation<>(BasePrivateResource.forPrivate(dir.toUri()));
        this.fileWithMsg = new AbsoluteLocation<>(
                BasePrivateResource.forPrivate(storageDir.toUri().resolve(FILE))
        );
    }

    @Test
    void objectExists() {
        createFileWithMessage();
        assertThat(storageService.objectExists(storageService.list(root).findFirst().get())).isTrue();
    }

    @Test
    void listEmpty() {
        Path nonExistingFile = storageDir.resolve(UUID.randomUUID().toString());
        AbsoluteLocation<PrivateResource> nonExistingFileLocation = new AbsoluteLocation<>(BasePrivateResource.forPrivate(nonExistingFile.toUri()));
        assertThat(storageService.list(nonExistingFileLocation).collect(Collectors.toList())).isEmpty();
    }

    @SneakyThrows
    @Test
    void resolveWithMkDirs() {
        Path nonExistingFolder = storageDir.resolve(UUID.randomUUID().toString());
        Path newFile = nonExistingFolder.resolve(UUID.randomUUID().toString());
        AbsoluteLocation<PrivateResource> newFileLocation = new AbsoluteLocation<>(BasePrivateResource.forPrivate(newFile.toUri()));

        try (OutputStream os = storageService.write(newFileLocation)) {
            os.write(MESSAGE.getBytes());
        }
        assertThat(storageService.objectExists(newFileLocation)).isTrue();
    }

    @SneakyThrows
    @Test
    void writeWithException() {
        Path realRoot = Paths.get("/");
        Path beforeRoot = realRoot.resolve("..");
        AbsoluteLocation<PrivateResource> newFileLocation = new AbsoluteLocation<>(BasePrivateResource.forPrivate(beforeRoot.toUri()));

        assertThatThrownBy(() -> {
            try (OutputStream os = storageService.write(newFileLocation)) {
                os.write(MESSAGE.getBytes());
            }
        });
    }

    @SneakyThrows
    @Test
    void readWithException() {
        Path beforeRoot = Paths.get("..").resolve(UUID.randomUUID().toString());
        AbsoluteLocation<PrivateResource> newFileLocation = new AbsoluteLocation<>(BasePrivateResource.forPrivate(beforeRoot.toUri()));

        assertThatThrownBy(() -> {
            try (InputStream is = storageService.read(newFileLocation)) {
                StringWriter writer = new StringWriter();
                String encoding = StandardCharsets.UTF_8.name();
                IOUtils.copy(is, writer, encoding);
                log.warn("found: " + writer.toString());
            }
        });
    }

    @SneakyThrows
    @Test
    void listDotFilesToo() {
        Path dotFile = storageDir.resolve(".dotfile");
        AbsoluteLocation<PrivateResource> newFileLocation = new AbsoluteLocation<>(BasePrivateResource.forPrivate(dotFile.toUri()));

        assertThat(storageService.list(root).collect(Collectors.toList())).isEmpty();
        try (OutputStream os = storageService.write(newFileLocation)) {
            os.write(MESSAGE.getBytes());
        }
        assertThat(storageService.list(root).collect(Collectors.toList())).isNotEmpty();
    }

    @Test
    void list() {
        createFileWithMessage();

        assertThat(storageService.list(root))
                .hasSize(1)
                .extracting(AbsoluteLocation::location)
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
