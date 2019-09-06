package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.business.impl.e2e.Const.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that validates basic functionality - storing data to inbox, privatespace, listing files, etc.
 */
@Slf4j
class BasicFunctionalityTest extends BaseE2ETest {

    private static final int LARGE_SIZE = 10 * 1024 * 1024 + 100;

    private StorageService storage;
    private Uri location;

    @ParameterizedTest
    @MethodSource("allStorages")
    void testDFSBasedProfileStorage(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);
        UserID userJohn = new UserID("john");
        assertThat(profileRetrievalService.userExists(userJohn)).isFalse();
        john = registerUser(userJohn.getValue());
        assertThat(profileRetrievalService.userExists(userJohn)).isTrue();
        profileRemovalService.deregister(john);
        assertThat(profileRetrievalService.userExists(userJohn)).isFalse();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("allStorages")
    void testUserIsRemovedWithFiles(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);
        UserID userJohn = new UserID("john");
        john = registerUser(userJohn.getValue());
        writeDataToPrivate(john, "root.txt", MESSAGE_ONE);
        writeDataToPrivate(john, "some/some.txt", MESSAGE_ONE);
        writeDataToPrivate(john, "some/another_one.txt", MESSAGE_ONE);
        writeDataToPrivate(john, "some/other/other.txt", MESSAGE_ONE);
        writeDataToPrivate(john, "different/data.txt", MESSAGE_ONE);

        profileRemovalService.deregister(john);

        assertRootDirIsEmpty(descriptor);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("allStorages")
    void testMultipleRecipientsSharing(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        UserIDAuth john = registerUser("john");
        UserIDAuth jane = registerUser("jane");
        UserIDAuth jamie = registerUser("jamie");

        String multiShareFile = "multishare.txt";
        try (OutputStream os = writeToInbox.write(WriteRequest.forDefaultPublic(
                ImmutableSet.of(john.getUserID(), jane.getUserID(), jamie.getUserID()),
                multiShareFile))
        ) {
            os.write(MESSAGE_ONE.getBytes());
        }

        assertThat(readFromInbox.read(ReadRequest.forDefaultPrivate(john, multiShareFile))).hasContent(MESSAGE_ONE);
        assertThat(readFromInbox.read(ReadRequest.forDefaultPrivate(jane, multiShareFile))).hasContent(MESSAGE_ONE);
        assertThat(readFromInbox.read(ReadRequest.forDefaultPrivate(jamie, multiShareFile))).hasContent(MESSAGE_ONE);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("allStorages")
    void testMultipleRecipientsSharingLargeChunk(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        UserIDAuth john = registerUser("john");
        UserIDAuth jane = registerUser("jane");
        UserIDAuth jamie = registerUser("jamie");

        String multiShareFile = "multishare.txt";
        byte[] bytes = new byte[LARGE_SIZE];
        ThreadLocalRandom.current().nextBytes(bytes);
        try (OutputStream os = writeToInbox.write(WriteRequest.forDefaultPublic(
                ImmutableSet.of(john.getUserID(), jane.getUserID(), jamie.getUserID()),
                multiShareFile))
        ) {
            os.write(bytes);
        }

        assertThat(readFromInbox.read(ReadRequest.forDefaultPrivate(john, multiShareFile)))
                .hasSameContentAs(new ByteArrayInputStream(bytes));
        assertThat(readFromInbox.read(ReadRequest.forDefaultPrivate(jane, multiShareFile)))
                .hasSameContentAs(new ByteArrayInputStream(bytes));
        assertThat(readFromInbox.read(ReadRequest.forDefaultPrivate(jamie, multiShareFile)))
                .hasSameContentAs(new ByteArrayInputStream(bytes));
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox(
            WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);

        AbsoluteLocation<ResolvedResource> privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane.getResource().asPrivate());

        sendToInbox(john.getUserID(), SHARED_FILE_PATH, privateContentJane);

        AbsoluteLocation<ResolvedResource> inboxJohn = getFirstFileInInbox(john);

        String result = readInboxUsingPrivateKey(john, inboxJohn.getResource().asPrivate());

        assertThat(result).isEqualTo(MESSAGE_ONE);
        assertThat(privateJane.getResource().asPrivate().decryptedPath())
                .extracting(Uri::toASCIIString).isEqualTo(PRIVATE_FILE_PATH);
        assertThat(privateJane.getResource().asPrivate().encryptedPath())
                .extracting(Uri::toASCIIString).isNotEqualTo(PRIVATE_FILE_PATH);
        validateInboxStructAndEncryption(inboxJohn);
        validatePrivateStructAndEncryption(privateJane);

        removeFromPrivate(jane, privateJane.getResource().asPrivate());
        removeFromInbox(john, inboxJohn.getResource().asPrivate());
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void listingValidation(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToPrivate(jane, "root.file", MESSAGE_ONE);
        writeDataToPrivate(jane, "level1/file", MESSAGE_ONE);
        writeDataToPrivate(jane, "level1/level2/file", MESSAGE_ONE);

        /*assertPrivateSpaceList(jane, "", "root.file", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, "./", "root.file", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, ".", "root.file", "level1/file", "level1/level2/file");

        assertPrivateSpaceList(jane, "root.file", "root.file");*/
        assertPrivateSpaceList(jane, "./root.file", "root.file");
/*
        assertPrivateSpaceList(jane, "level1", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, "level1/", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, "./level1", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, "./level1/", "level1/file", "level1/level2/file");

        assertPrivateSpaceList(jane, "./level1/level2", "level1/level2/file");
        assertPrivateSpaceList(jane, "./level1/level2/", "level1/level2/file");
        assertPrivateSpaceList(jane, "level1/level2", "level1/level2/file");
        assertPrivateSpaceList(jane, "level1/level2/", "level1/level2/file");*/
    }


    @ParameterizedTest
    @MethodSource("allStorages")
    void listingInboxValidation(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToInbox(jane, "root.file", MESSAGE_ONE);
        writeDataToInbox(jane, "level1/file", MESSAGE_ONE);
        writeDataToInbox(jane, "level1/level2/file", MESSAGE_ONE);

        assertInboxSpaceList(jane, "", "root.file", "level1/file", "level1/level2/file");
        assertInboxSpaceList(jane, "./", "root.file", "level1/file", "level1/level2/file");
        assertInboxSpaceList(jane, ".", "root.file", "level1/file", "level1/level2/file");

        assertInboxSpaceList(jane, "root.file", "root.file");
        assertInboxSpaceList(jane, "./root.file", "root.file");

        assertInboxSpaceList(jane, "level1", "level1/file", "level1/level2/file");
        assertInboxSpaceList(jane, "level1/", "level1/file", "level1/level2/file");
        assertInboxSpaceList(jane, "./level1", "level1/file", "level1/level2/file");
        assertInboxSpaceList(jane, "./level1/", "level1/file", "level1/level2/file");

        assertInboxSpaceList(jane, "./level1/level2", "level1/level2/file");
        assertInboxSpaceList(jane, "./level1/level2/", "level1/level2/file");
        assertInboxSpaceList(jane, "level1/level2", "level1/level2/file");
        assertInboxSpaceList(jane, "level1/level2/", "level1/level2/file");
    }

    @SneakyThrows
    private void validateInboxStructAndEncryption(AbsoluteLocation<ResolvedResource> expectedInboxResource) {
        List<AbsoluteLocation<ResolvedResource>> inbox = listFiles(it -> it.contains(INBOX_COMPONENT));

        assertThat(inbox).hasSize(1);
        AbsoluteLocation<ResolvedResource> foundResource = inbox.get(0);
        assertThat(foundResource.location()).isEqualTo(expectedInboxResource.location());
        // no path encryption for inbox:
        assertThat(foundResource.location().getPath()).asString().contains(SHARED_FILE);
        // validate encryption on high-level:
        assertThat(storage.read(foundResource)).asString().doesNotContain(MESSAGE_ONE);
    }

    @SneakyThrows
    private void validatePrivateStructAndEncryption(AbsoluteLocation<ResolvedResource> expectedPrivateResource) {
        List<AbsoluteLocation<ResolvedResource>> privateFiles = listFiles(
                it -> it.contains(PRIVATE_FILES_COMPONENT));

        assertThat(privateFiles).hasSize(1);
        AbsoluteLocation<ResolvedResource> foundResource = privateFiles.get(0);
        assertThat(foundResource.location()).isEqualTo(expectedPrivateResource.location());

        // validate encryption on high-level:
        assertThat(foundResource.toString()).doesNotContain(PRIVATE_FILE);
        assertThat(foundResource.toString()).doesNotContain(FOLDER);
        assertThat(storage.read(foundResource)).asString().doesNotContain(MESSAGE_ONE);
    }

    @SneakyThrows
    private List<AbsoluteLocation<ResolvedResource>> listFiles(Predicate<String> pattern) {
        return storage.list(new AbsoluteLocation<>(BasePrivateResource.forPrivate(location)))
                .filter(it -> !it.location().toASCIIString().startsWith("."))
                .filter(it -> pattern.test(it.location().toASCIIString()))
                .collect(Collectors.toList());
    }

    private void init(WithStorageProvider.StorageDescriptor descriptor) {
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);

        this.location = descriptor.getLocation();
        this.storage = descriptor.getStorageService().get();
    }
}
