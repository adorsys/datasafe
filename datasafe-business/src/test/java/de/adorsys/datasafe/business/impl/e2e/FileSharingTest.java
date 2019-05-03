package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.base.Predicate;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeServices;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

@Slf4j
class FileSharingTest extends BaseE2ETest {

    private static final String MESSAGE_ONE = "Hello here";
    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";
    private static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;

    private static final String SHARED_FILE = "hello.txt";
    private static final String SHARED_FILE_PATH = SHARED_FILE;

    private FileSystemStorage storage;
    private Path location;

    @BeforeEach
    void init(@TempDir Path location) {
        this.location = location;
        this.storage = new FileSystemStorage(location);

        this.services = DaggerDefaultDocusafeServices
                .builder()
                .storageList(storage::listFiles)
                .storageRead(storage::readFile)
                .storageWrite(storage::writeFile)
                .build();
    }

    @Test
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox(@TempDir Path dfsLocation) {
        tempDir = dfsLocation;

        registerJohnAndJane();

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);

        PrivateResource privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane);

        sendToInbox(jane.getUserID(), john.getUserID(), SHARED_FILE_PATH, privateContentJane);

        PrivateResource inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn);

        assertThat(result).isEqualTo(MESSAGE_ONE);
        validateInboxEncrypted();
        validatePrivateEncrypted();
    }

    private void validateInboxEncrypted() {
        List<Path> inbox = listFiles(it -> it.contains(INBOX_COMPONENT));

        assertThat(inbox).hasSize(1);
        assertThat(inbox.get(0).toString()).contains(SHARED_FILE);
        assertThat(contentOf(inbox.get(0).toFile())).doesNotContain(MESSAGE_ONE);
    }

    private void validatePrivateEncrypted() {
        List<Path> inbox = listFiles(it -> it.contains(PRIVATE_COMPONENT) && it.contains(BUCKET_COMPONENT));

        assertThat(inbox).hasSize(1);
        assertThat(inbox.get(0).toString()).doesNotContain(PRIVATE_FILE);
        assertThat(inbox.get(0).toString()).doesNotContain(FOLDER);
        assertThat(contentOf(inbox.get(0).toFile())).doesNotContain(MESSAGE_ONE);
    }

    @SneakyThrows
    private List<Path> listFiles(Predicate<String> pattern) {
        return Files.walk(location)
                .filter(it -> !it.startsWith("."))
                .filter(it -> !it.toFile().isDirectory())
                .filter(it -> pattern.apply(it.toString()))
                .collect(Collectors.toList());
    }
}
