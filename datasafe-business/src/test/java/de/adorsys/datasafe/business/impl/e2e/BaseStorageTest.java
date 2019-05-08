package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.business.api.types.action.ListRequest.forPrivate;
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

    private static final int NUMBER_OF_TEST_USERS = 5;//10;
    private static final int NUMBER_OF_TEST_FILES = 10;//100;
    private static final int EXPECTED_NUMBER_OF_FILES_PER_USER = NUMBER_OF_TEST_FILES;

    protected StorageService storage;
    protected URI location;

    @Test
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox() {

        registerJohnAndJane(location);

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);

        AbsoluteResourceLocation<PrivateResource> privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane.getResource());

        sendToInbox(john.getUserID(), SHARED_FILE_PATH, privateContentJane);

        AbsoluteResourceLocation<PrivateResource> inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn.getResource());

        assertThat(result).isEqualTo(MESSAGE_ONE);
        assertThat(privateJane.getResource().decryptedPath()).asString().isEqualTo(PRIVATE_FILE_PATH);
        assertThat(privateJane.getResource().encryptedPath()).asString().isNotEqualTo(PRIVATE_FILE_PATH);
        validateInboxStructAndEncryption(inboxJohn);
        validatePrivateStructAndEncryption(privateJane);
    }

    @SneakyThrows
    private void validateInboxStructAndEncryption(AbsoluteResourceLocation<PrivateResource> expectedInboxResource) {
        List<AbsoluteResourceLocation<PrivateResource>> inbox = listFiles(it -> it.contains(INBOX_COMPONENT));

        assertThat(inbox).hasSize(1);
        AbsoluteResourceLocation<PrivateResource> foundResource = inbox.get(0);
        assertThat(foundResource.location()).isEqualTo(expectedInboxResource.location());
        // no path encryption for inbox:
        assertThat(foundResource.toString()).contains(SHARED_FILE);
        // validate encryption on high-level:
        assertThat(storage.read(foundResource)).asString().doesNotContain(MESSAGE_ONE);
    }

    @SneakyThrows
    private void validatePrivateStructAndEncryption(AbsoluteResourceLocation<PrivateResource> expectedPrivateResource) {
        List<AbsoluteResourceLocation<PrivateResource>> privateFiles = listFiles(
                it -> it.contains(PRIVATE_FILES_COMPONENT));

        assertThat(privateFiles).hasSize(1);
        AbsoluteResourceLocation<PrivateResource> foundResource = privateFiles.get(0);
        assertThat(foundResource.location()).isEqualTo(expectedPrivateResource.location());
        // validate encryption on high-level:
        assertThat(foundResource.toString()).doesNotContain(PRIVATE_FILE);
        assertThat(foundResource.toString()).doesNotContain(FOLDER);
        assertThat(storage.read(foundResource)).asString().doesNotContain(MESSAGE_ONE);
    }

    @SneakyThrows
    private List<AbsoluteResourceLocation<PrivateResource>> listFiles(Predicate<String> pattern) {
        return storage.list(new AbsoluteResourceLocation<>(DefaultPrivateResource.forPrivate(location)))
                .filter(it -> !it.location().toString().startsWith("."))
                .filter(it -> pattern.test(it.location().toString()))
                .collect(Collectors.toList());
    }


    CountDownLatch fileSaveCountDown = new CountDownLatch(NUMBER_OF_TEST_USERS * NUMBER_OF_TEST_FILES);

    @Test
    @SneakyThrows
    public void WriteToPrivateListPrivateInDifferentThreads() {
        String path = "folder2";

        log.trace("*** Starting write threads ***");
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            UserIDAuth john = registerUser("john_" + i, location);
            log.debug("Registered user: {}", john.getUserID().getValue());

            AtomicInteger counter = new AtomicInteger();

            for (int j = 0; j < NUMBER_OF_TEST_FILES; j++) {
                new Thread(() -> {
                    try {
                        log.trace("Start thread: {} for user: {}",
                                Thread.currentThread().getName(), john.getUserID().getValue());

                        String filePath = path + "/" + counter.incrementAndGet() + ".txt";

                        log.debug("Saving file: {}", filePath);
                        writeTextToFileForUser(john, filePath, MESSAGE_ONE, fileSaveCountDown);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        log.trace("*** Main thread waiting for all threads ***");
        fileSaveCountDown.await();
        log.trace("*** All threads are finished work ***");

        log.trace("*** Starting read info saved earlier *** ");
        for (int i = 0; i < NUMBER_OF_TEST_USERS; i++) {
            UserIDAuth user = createJohnTestUser(i);

            List<AbsoluteResourceLocation<PrivateResource>> resourceList = services.privateService().list(forPrivate(user, "./")).collect(Collectors.toList());
            log.debug("Read files for user: " + user.getUserID().getValue());

            assertThat(resourceList.size()).isEqualTo(EXPECTED_NUMBER_OF_FILES_PER_USER);
            resourceList.forEach(item -> {
                String content = extractFileContent(user, item.getResource());

                log.debug("Content: " + content);
                assertThat(content).isEqualTo(MESSAGE_ONE);
            });
        }
    }
}
