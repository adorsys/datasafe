package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
abstract class StorageTest extends BaseE2ETest {

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

        PrivateResource privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane);

        sendToInbox(jane.getUserID(), john.getUserID(), SHARED_FILE_PATH, privateContentJane);

        PrivateResource inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn);

        assertThat(result).isEqualTo(MESSAGE_ONE);
        assertThat(privateJane.decryptedPath()).asString().isEqualTo(PRIVATE_FILE_PATH);
        assertThat(privateJane.encryptedPath()).asString().isNotEqualTo(PRIVATE_FILE_PATH);
       // validateInboxEncrypted();
      //  validatePrivateEncrypted();
    }

   /* private void validateInboxEncrypted() {
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
        storage.list(new DefaultPrivateResource(location.toUri()))
                .map(it -> it.location())
                .filter(it -> !it.startsWith("."))
                .filter(it -> !it.toFile().isDirectory())
                .filter(it -> pattern.apply(it.toString()))
                .collect(Collectors.toList());
    }*/
}
