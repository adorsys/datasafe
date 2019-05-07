package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.CharStreams;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
abstract class BaseStorageTest extends BaseE2ETest {

    private static final String MESSAGE_ONE = "Hello here";
    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";
    private static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;

    private static final String SHARED_FILE = "hello.txt";
    private static final String SHARED_FILE_PATH = SHARED_FILE;

    protected StorageService storage;
    protected URI location;

    @Test
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox() {

        registerJohnAndJane(location);

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);

        AbsoluteResourceLocation<PrivateResource> privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane.getResource());

        sendToInbox(jane.getUserID(), john.getUserID(), SHARED_FILE_PATH, privateContentJane);

        AbsoluteResourceLocation<PrivateResource> inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn.getResource());

        assertThat(result).isEqualTo(MESSAGE_ONE);
        assertThat(privateJane.getResource().decryptedPath()).asString().isEqualTo(PRIVATE_FILE_PATH);
        assertThat(privateJane.getResource().encryptedPath()).asString().isNotEqualTo(PRIVATE_FILE_PATH);
        validateInboxEncrypted(inboxJohn);
        validatePrivateEncrypted(privateJane);
    }

    @SneakyThrows
    private void validateInboxEncrypted(AbsoluteResourceLocation<PrivateResource> expectedInboxResource) {
        List<AbsoluteResourceLocation<PrivateResource>> inbox = listFiles(it -> it.contains(INBOX_COMPONENT));

        assertThat(inbox).hasSize(1);
        assertThat(inbox.get(0).location()).isEqualTo(expectedInboxResource.location());
        assertThat(inbox.get(0).toString()).contains(SHARED_FILE);
        assertThat(
                CharStreams.toString(new InputStreamReader(storage.read(inbox.get(0))))
        ).doesNotContain(MESSAGE_ONE);
    }

    @SneakyThrows
    private void validatePrivateEncrypted(AbsoluteResourceLocation<PrivateResource> expectedPrivateResource) {
        List<AbsoluteResourceLocation<PrivateResource>> privateFiles = listFiles(
                it -> it.contains(PRIVATE_FILES_COMPONENT));

        assertThat(privateFiles).hasSize(1);
        assertThat(privateFiles.get(0).location()).isEqualTo(expectedPrivateResource.location());
        assertThat(privateFiles.get(0).toString()).doesNotContain(PRIVATE_FILE);
        assertThat(privateFiles.get(0).toString()).doesNotContain(FOLDER);
        assertThat(
                CharStreams.toString(new InputStreamReader(storage.read(privateFiles.get(0))))
        ).doesNotContain(MESSAGE_ONE);
    }

    @SneakyThrows
    private List<AbsoluteResourceLocation<PrivateResource>> listFiles(Predicate<String> pattern) {
        return storage.list(new AbsoluteResourceLocation<>(DefaultPrivateResource.forPrivate(location)))
                .filter(it -> !it.location().toString().startsWith("."))
                .filter(it -> pattern.test(it.location().toString()))
                .collect(Collectors.toList());
    }
}
