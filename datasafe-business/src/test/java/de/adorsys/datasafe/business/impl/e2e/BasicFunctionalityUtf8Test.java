package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that validates basic functionality - storing data to inbox, privatespace, listing files, etc. using UTF-8 paths.
 */
@Slf4j
class BasicFunctionalityUtf8Test extends BaseE2ETest {

    private static final String MESSAGE_ONE = "Hello here 1";
    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";
    private static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;

    @ParameterizedTest
    @MethodSource("allStorages")
    void readPrivateContentWithUnicode(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        jane = registerUser("jane");


        String unicodeMessage = "привет мир!";
        writeDataToPrivate(jane, PRIVATE_FILE_PATH, unicodeMessage);

        AbsoluteLocation<ResolvedResource> privateJane = getFirstFileInPrivate(jane);

        String privateContentJane = readPrivateUsingPrivateKey(jane, privateJane.getResource().asPrivate());

        assertThat(privateContentJane).isEqualTo(unicodeMessage);
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void readPrivateContentWithUnicodeUsingUnicodePath(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        jane = registerUser("jane");


        String unicodeMessage = "привет мир!";
        writeDataToPrivate(jane, " привет/prüfungsdokument/файл с пробелом.док", unicodeMessage);

        String privateContentJane = readPrivateUsingPrivateKey(
                jane,
                BasePrivateResource.forPrivate(" привет/prüfungsdokument/файл с пробелом.док")
        );

        assertThat(privateContentJane).isEqualTo(unicodeMessage);
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void listingPrivatePathWithSpaces(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToPrivate(jane, "file_one.doc", MESSAGE_ONE);
        writeDataToPrivate(jane, "level 1/ test file", MESSAGE_ONE);
        writeDataToPrivate(jane, "level 1/level 2/test file", MESSAGE_ONE);

        assertPrivateSpaceList(jane, "", "file_one.doc", "level 1/ test file", "level 1/level 2/test file");
        assertPrivateSpaceList(jane, "./", "file_one.doc", "level 1/ test file", "level 1/level 2/test file");
        assertPrivateSpaceList(jane, ".", "file_one.doc", "level 1/ test file", "level 1/level 2/test file");

        assertPrivateSpaceList(jane, "file_one.doc", "file_one.doc");
        assertPrivateSpaceList(jane, "./file_one.doc", "file_one.doc");
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void listingPrivatePathWithUnicode(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToPrivate(jane, "prüfungsdokument.doc", MESSAGE_ONE);
        writeDataToPrivate(jane, "уровень1/?файл", MESSAGE_ONE);
        writeDataToPrivate(jane, "уровень1/уровень2=/&файл", MESSAGE_ONE);

        assertPrivateSpaceList(jane, "", "prüfungsdokument.doc", "уровень1/?файл", "уровень1/уровень2=/&файл");
        assertPrivateSpaceList(jane, "./", "prüfungsdokument.doc", "уровень1/?файл", "уровень1/уровень2=/&файл");
        assertPrivateSpaceList(jane, ".", "prüfungsdokument.doc", "уровень1/?файл", "уровень1/уровень2=/&файл");

        assertPrivateSpaceList(jane, "prüfungsdokument.doc", "prüfungsdokument.doc");
        assertPrivateSpaceList(jane, "./prüfungsdokument.doc", "prüfungsdokument.doc");
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void readInboxContentWithUnicodeUsingUnicodePath(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        jane = registerUser("jane");


        String unicodeMessage = "привет мир!";
        writeDataToInbox(jane, " привет/prüfungsdokument/файл с пробелом.док", unicodeMessage);

        String inboxContentJane = readInboxUsingPrivateKey(
                jane,
                BasePrivateResource.forPrivate(" привет/prüfungsdokument/файл с пробелом.док")
        );

        assertThat(inboxContentJane).isEqualTo(unicodeMessage);
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void readInboxContentWithUnicode(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        jane = registerUser("jane");


        String unicodeMessage = "привет мир!";
        writeDataToInbox(jane, PRIVATE_FILE_PATH, unicodeMessage);

        AbsoluteLocation<ResolvedResource> privateJane = getFirstFileInInbox(jane);

        String inboxContentJane = readInboxUsingPrivateKey(jane, privateJane.getResource().asPrivate());

        assertThat(inboxContentJane).isEqualTo(unicodeMessage);
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void listingInboxPathWithSpaces(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToInbox(jane, "file_one.doc", MESSAGE_ONE);
        writeDataToInbox(jane, "level 1/ test file", MESSAGE_ONE);
        writeDataToInbox(jane, "level 1/level 2/test file", MESSAGE_ONE);

        assertInboxSpaceList(jane, "", "file_one.doc", "level 1/ test file", "level 1/level 2/test file");
        assertInboxSpaceList(jane, "./", "file_one.doc", "level 1/ test file", "level 1/level 2/test file");
        assertInboxSpaceList(jane, ".", "file_one.doc", "level 1/ test file", "level 1/level 2/test file");

        assertInboxSpaceList(jane, "file_one.doc", "file_one.doc");
        assertInboxSpaceList(jane, "./file_one.doc", "file_one.doc");
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void listingInboxPathWithUnicode(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToInbox(jane, "prüfungsdokument.doc", MESSAGE_ONE);
        writeDataToInbox(jane, "уровень1/?файл", MESSAGE_ONE);
        writeDataToInbox(jane, "уровень1/уровень2=/&файл", MESSAGE_ONE);

        assertInboxSpaceList(jane, "", "prüfungsdokument.doc", "уровень1/?файл", "уровень1/уровень2=/&файл");
        assertInboxSpaceList(jane, "./", "prüfungsdokument.doc", "уровень1/?файл", "уровень1/уровень2=/&файл");
        assertInboxSpaceList(jane, ".", "prüfungsdokument.doc", "уровень1/?файл", "уровень1/уровень2=/&файл");

        assertInboxSpaceList(jane, "prüfungsdokument.doc", "prüfungsdokument.doc");
        assertInboxSpaceList(jane, "./prüfungsdokument.doc", "prüfungsdokument.doc");
    }

    private void init(StorageDescriptor descriptor) {
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }
}
