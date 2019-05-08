package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

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

        AbsoluteResourceLocation<PrivateResource> privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane.getResource());

        sendToInbox(john.getUserID(), SHARED_FILE_PATH, privateContentJane);

        AbsoluteResourceLocation<PrivateResource> inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn.getResource());

        assertThat(result).isEqualTo(MESSAGE_ONE);
        assertThat(privateJane.getResource().decryptedPath()).asString().isEqualTo(PRIVATE_FILE_PATH);
        assertThat(privateJane.getResource().encryptedPath()).asString().isNotEqualTo(PRIVATE_FILE_PATH);
    }
}
