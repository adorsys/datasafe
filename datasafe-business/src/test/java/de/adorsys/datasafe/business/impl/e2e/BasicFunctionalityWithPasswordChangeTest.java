package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;

import java.io.OutputStream;
import java.security.UnrecoverableKeyException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static de.adorsys.datasafe.business.impl.e2e.Const.MESSAGE_ONE;
import static de.adorsys.datasafe.business.impl.e2e.Const.PRIVATE_FILE_PATH;
import static de.adorsys.datasafe.business.impl.e2e.Const.SHARED_FILE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BasicFunctionalityWithPasswordChangeTest extends BaseE2ETest {

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("allStorages")
    void testUserIsRemovedWithFiles(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);
        UserID userJohn = new UserID("john");
        john = registerUser(userJohn.getValue());
        writeDataToPrivate(john, "root.txt", MESSAGE_ONE);
        writeDataToPrivate(john, "some/some.txt", MESSAGE_ONE);

        john = checkUpdatedCredsWorkAndOldDont(john, ReadKeyPasswordTestFactory.getForString("Some other"), auth -> {
            writeDataToPrivate(auth, "some/other/other.txt", MESSAGE_ONE);
            writeDataToPrivate(auth, "different/data.txt", MESSAGE_ONE);
        });

        john = checkUpdatedCredsWorkAndOldDont(
                john,
                ReadKeyPasswordTestFactory.getForString("Some another"),
                auth -> profileRemovalService.deregister(auth)
        );

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
        multishareFiles(john, jane, jamie, multiShareFile);

        Stream.of(john, jane, jamie).forEach(
                it -> checkUpdatedCredsWorkAndOldDont(
                        it,
                        ReadKeyPasswordTestFactory.getForString(UUID.randomUUID().toString()),
                        auth -> assertThat(readFromInbox.read(ReadRequest.forDefaultPrivate(auth, multiShareFile)))
                                .hasContent(MESSAGE_ONE))
        );
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void testWriteToPrivateListPrivateReadPrivateAndSendToAndReadFromInbox(
            WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);

        jane = checkUpdatedCredsWorkAndOldDont(
                jane,
                ReadKeyPasswordTestFactory.getForString("Another passwd1"),
                this::getFirstFileInPrivate
        );

        AbsoluteLocation<ResolvedResource> privateJane = getFirstFileInPrivate(jane);

        jane = checkUpdatedCredsWorkAndOldDont(
                jane,
                ReadKeyPasswordTestFactory.getForString("Another passwd2"),
                auth -> readPrivateUsingPrivateKey(auth, privateJane.getResource().asPrivate())
        );

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane.getResource().asPrivate());

        sendToInbox(john.getUserID(), SHARED_FILE_PATH, privateContentJane);

        AbsoluteLocation<ResolvedResource> inboxJohn = getFirstFileInInbox(john);

        john = checkUpdatedCredsWorkAndOldDont(
                john,
                ReadKeyPasswordTestFactory.getForString("Another passwd4"),
                auth -> readInboxUsingPrivateKey(auth, inboxJohn.getResource().asPrivate())
        );

        String result = readInboxUsingPrivateKey(john, inboxJohn.getResource().asPrivate());

        assertThat(result).isEqualTo(MESSAGE_ONE);
        assertThat(privateJane.getResource().asPrivate().decryptedPath())
                .extracting(Uri::toASCIIString).isEqualTo(PRIVATE_FILE_PATH);
        assertThat(privateJane.getResource().asPrivate().encryptedPath())
                .extracting(Uri::toASCIIString).isNotEqualTo(PRIVATE_FILE_PATH);
    }


    @ParameterizedTest
    @MethodSource("allStorages")
    void listingValidation(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToPrivate(jane, "root.file", MESSAGE_ONE);
        writeDataToPrivate(jane, "level1/file", MESSAGE_ONE);

        jane = checkUpdatedCredsWorkAndOldDont(
                jane,
                ReadKeyPasswordTestFactory.getForString("Another passwd1"),
                auth -> writeDataToPrivate(auth, "level1/level2/file", MESSAGE_ONE)
        );

        jane = checkUpdatedCredsWorkAndOldDont(
                jane,
                ReadKeyPasswordTestFactory.getForString("Another passwd2"),
                auth -> {
                    assertPrivateSpaceList(auth, "", "root.file", "level1/file", "level1/level2/file");
                    assertPrivateSpaceList(auth, "./", "root.file", "level1/file", "level1/level2/file");
                    assertPrivateSpaceList(auth, ".", "root.file", "level1/file", "level1/level2/file");
                }
        );

        jane = checkUpdatedCredsWorkAndOldDont(
                jane,
                ReadKeyPasswordTestFactory.getForString("Another passwd3"),
                auth -> {
                    assertPrivateSpaceList(auth, "root.file", "root.file");
                    assertPrivateSpaceList(auth, "./root.file", "root.file");
                }
        );

        jane = checkUpdatedCredsWorkAndOldDont(
                jane,
                ReadKeyPasswordTestFactory.getForString("Another passwd4"),
                auth -> {
                    assertPrivateSpaceList(auth, "level1", "level1/file", "level1/level2/file");
                    assertPrivateSpaceList(auth, "level1/", "level1/file", "level1/level2/file");
                }
        );

        jane = checkUpdatedCredsWorkAndOldDont(
                jane,
                ReadKeyPasswordTestFactory.getForString("Another passwd5"),
                auth -> {
                    assertPrivateSpaceList(auth, "./level1", "level1/file", "level1/level2/file");
                    assertPrivateSpaceList(auth, "./level1/", "level1/file", "level1/level2/file");
                }
        );

        jane = checkUpdatedCredsWorkAndOldDont(
                jane,
                ReadKeyPasswordTestFactory.getForString("Another passwd6"),
                auth -> {
                    assertPrivateSpaceList(auth, "./level1/level2", "level1/level2/file");
                    assertPrivateSpaceList(auth, "./level1/level2/", "level1/level2/file");
                    assertPrivateSpaceList(auth, "level1/level2", "level1/level2/file");
                    assertPrivateSpaceList(auth, "level1/level2/", "level1/level2/file");
                }
        );
    }

    @SneakyThrows
    private void multishareFiles(UserIDAuth userOne, UserIDAuth userTwo, UserIDAuth userThree, String multiShareFile) {
        try (OutputStream os = writeToInbox.write(WriteRequest.forDefaultPublic(
                ImmutableSet.of(userOne.getUserID(), userTwo.getUserID(), userThree.getUserID()),
                multiShareFile))
        ) {
            os.write(MESSAGE_ONE.getBytes());
        }
    }

    private UserIDAuth checkUpdatedCredsWorkAndOldDont(UserIDAuth auth,
                                                       ReadKeyPassword newPassword,
                                                       Consumer<UserIDAuth> withAuth) {
        profileUpdatingService.updateReadKeyPassword(auth, newPassword);
        assertThrows(UnrecoverableKeyException.class, () -> withAuth.accept(auth));
        UserIDAuth newAuth = new UserIDAuth(auth.getUserID(), newPassword);
        withAuth.accept(newAuth);
        return newAuth;
    }

    private void init(WithStorageProvider.StorageDescriptor descriptor) {
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }
}
