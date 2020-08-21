package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.metainfo.version.impl.version.types.DFSVersion;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.Version;
import de.adorsys.datasafe.types.api.resource.Versioned;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates software versioned operations.
 */
@Slf4j
public class VersionedDataTest extends BaseE2ETest {

    private static final String MESSAGE_ONE = "Hello here 1";
    private static final String MESSAGE_TWO = "Hello here 2";
    private static final String MESSAGE_THREE = "Hello here 3";

    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";
    private static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;

    private VersionedDatasafeServices versionedDocusafeServices;

    @ParameterizedTest
    @MethodSource("allLocalDefaultStorages")
    void testVersionedWriteTopLevel(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation();

        String readingResult = readPrivateUsingPrivateKey(jane, BasePrivateResource.forPrivate(PRIVATE_FILE_PATH));

        assertThat(readingResult).isEqualTo(MESSAGE_THREE);
        validateThereAreVersions(jane, 3);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("allLocalDefaultStorages")
    void testUserIsRemovedWithFiles(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);
        UserID userJohn = new UserID("john");
        john = registerUser(userJohn.getValue());
        writeDataToPrivate(john, "root.txt", MESSAGE_ONE);
        writeDataToPrivate(john, "some/some.txt", MESSAGE_ONE);
        writeDataToPrivate(john, "some/other/other.txt", MESSAGE_ONE);

        profileRemovalService.deregister(john);

        assertRootDirIsEmpty(descriptor);
    }

    @ParameterizedTest
    @MethodSource("allLocalDefaultStorages")
    void testVersionedWriteUsingAbsoluteAccess(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);
        registerAndDoWritesWithDiffMessageInSameLocation();
        Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version> first = firstOfVersionedListRoot(jane);
        String directResult = readPrivateUsingPrivateKey(jane, first.stripVersion().asPrivate());
        assertThat(directResult).isEqualTo(MESSAGE_THREE);
        assertThat(first.stripVersion().asPrivate().decryptedPath().asString()).contains(PRIVATE_FILE_PATH);
        validateThereAreVersions(jane, 3);
    }

    @ParameterizedTest
    @MethodSource("allLocalDefaultStorages")
    void testVersionedRemove(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation();

        Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version> first = firstOfVersionedListRoot(jane);
        versionedDocusafeServices.latestPrivate().remove(
                RemoveRequest.forPrivate(jane, first.stripVersion().asPrivate())
        );

        assertThat(listRoot(jane)).isEmpty();
        validateThereAreVersions(jane, 3);
    }

    @ParameterizedTest
    @MethodSource("allLocalDefaultStorages")
    void testVersionsOf(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation();

        List<Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion>> versionedResource =
                versionedDocusafeServices.versionInfo()
                        .versionsOf(ListRequest.forDefaultPrivate(jane, "./"))
                        .collect(Collectors.toList());

        assertThat(versionedResource).hasSize(3);
        assertThat(versionedResource)
                .extracting(Versioned::stripVersion)
                .extracting(ResourceLocation::location)
                .extracting(Uri::toASCIIString)
                .containsExactly(PRIVATE_FILE_PATH, PRIVATE_FILE_PATH, PRIVATE_FILE_PATH);
    }

    @ParameterizedTest
    @MethodSource("allLocalDefaultStorages")
    void testVersionsOfDirectPath(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation();

        List<Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion>> versionedResource =
                versionedDocusafeServices.versionInfo()
                        .versionsOf(ListRequest.forDefaultPrivate(jane, PRIVATE_FILE_PATH))
                        .collect(Collectors.toList());

        assertThat(versionedResource).hasSize(3);
        assertThat(versionedResource)
                .extracting(Versioned::stripVersion)
                .extracting(ResourceLocation::location)
                .extracting(Uri::toASCIIString)
                .containsExactly(PRIVATE_FILE_PATH, PRIVATE_FILE_PATH, PRIVATE_FILE_PATH);
    }

    // this test imitates removal of old file versions
    @ParameterizedTest
    @MethodSource("allLocalDefaultStorages")
    void testOldRemoval(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation();

        List<Versioned<AbsoluteLocation<ResolvedResource>, ResolvedResource, DFSVersion>> versionedResource =
                versionedDocusafeServices.versionInfo()
                        .listJoinedWithLatest(ListRequest.forDefaultPrivate(jane, "./"))
                        .collect(Collectors.toList());

        // remove `old` versions
        List<ResolvedResource> toRemove = versionedResource.stream()
                .filter(it -> !it.stripVersion().location().equals(it.absolute().location()))
                // ideally you want to filter out by time as well - example: older than 1 hour
                .map(Versioned::absolute)
                .map(AbsoluteLocation::getResource)
                .collect(Collectors.toList());
        toRemove.forEach(it ->
                versionedDocusafeServices.privateService().remove(RemoveRequest.forPrivate(jane, it.asPrivate()))
        );

        // expect that latest file is still present
        String readingResult = readPrivateUsingPrivateKey(jane, BasePrivateResource.forPrivate(PRIVATE_FILE_PATH));
        assertThat(readingResult).isEqualTo(MESSAGE_THREE);
        validateThereAreVersions(jane, 1);
    }

    @ParameterizedTest
    @MethodSource("allLocalDefaultStorages")
    void listingValidation(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerJohnAndJane();

        writeDataToPrivate(jane, "root.file", MESSAGE_ONE);
        writeDataToPrivate(jane, "root.file", MESSAGE_ONE);
        writeDataToPrivate(jane, "root.file", MESSAGE_THREE);
        writeDataToPrivate(jane, "level1/file", MESSAGE_ONE);
        writeDataToPrivate(jane, "level1/level2/file", MESSAGE_ONE);
        writeDataToPrivate(jane, "level1/level2/file", MESSAGE_THREE);

        writeDataToPrivate(jane, "root.file", MESSAGE_ONE);
        writeDataToPrivate(jane, "level1/file", MESSAGE_ONE);
        writeDataToPrivate(jane, "level1/level2/file", MESSAGE_ONE);

        assertPrivateSpaceList(jane, "", "root.file", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, "./", "root.file", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, ".", "root.file", "level1/file", "level1/level2/file");

        assertPrivateSpaceList(jane, "root.file", "root.file");
        assertPrivateSpaceList(jane, "./root.file", "root.file");

        assertPrivateSpaceList(jane, "level1", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, "level1/", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, "./level1", "level1/file", "level1/level2/file");
        assertPrivateSpaceList(jane, "./level1/", "level1/file", "level1/level2/file");

        assertPrivateSpaceList(jane, "./level1/level2", "level1/level2/file");
        assertPrivateSpaceList(jane, "./level1/level2/", "level1/level2/file");
        assertPrivateSpaceList(jane, "level1/level2", "level1/level2/file");
        assertPrivateSpaceList(jane, "level1/level2/", "level1/level2/file");
    }

    @Override
    @SneakyThrows
    protected void writeDataToPrivate(UserIDAuth auth, String path, String data) {
        try (OutputStream stream = writeToPrivate.write(WriteRequest.forDefaultPrivate(auth, path))) {
            stream.write(data.getBytes());
        }
    }

    @Override
    @SneakyThrows
    protected String readPrivateUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream dataStream = readFromPrivate.read(ReadRequest.forPrivate(user, location))) {
            ByteStreams.copy(dataStream, outputStream);
        }

        return new String(outputStream.toByteArray());
    }

    @SneakyThrows
    protected String readRawPrivateUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream dataStream =
                     versionedDocusafeServices.privateService().read(ReadRequest.forPrivate(user, location))) {
            ByteStreams.copy(dataStream, outputStream);
        }

        return new String(outputStream.toByteArray());
    }

    @Override
    protected AbsoluteLocation<ResolvedResource> getFirstFileInPrivate(UserIDAuth owner) {
        List<AbsoluteLocation<ResolvedResource>> files = listRoot(owner).collect(Collectors.toList());

        log.info("{} has {} in PRIVATE", owner.getUserID().getValue(), files);
        return files.get(0);
    }

    private Stream<AbsoluteLocation<ResolvedResource>> listRoot(UserIDAuth owner) {
        return listPrivate.list(ListRequest.forDefaultPrivate(owner, "./"));
    }

    private Stream<Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version>> versionedListRoot(
            UserIDAuth owner) {
        return versionedDocusafeServices.latestPrivate().listWithDetails(ListRequest.forDefaultPrivate(owner, "./"));
    }

    private void init(WithStorageProvider.StorageDescriptor descriptor) {
        VersionedDatasafeServices datasafeServices = DatasafeServicesProvider
                .versionedDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());

        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
        this.versionedDocusafeServices = datasafeServices;
    }


    private void registerAndDoWritesWithDiffMessageInSameLocation() {
        registerJohnAndJane();

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);
        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_TWO);
        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_THREE);
    }

    private void validateThereAreVersions(UserIDAuth user, int versionCount) {
        assertThat(
                versionedDocusafeServices.privateService().list(ListRequest.forDefaultPrivate(user, "./"))
        ).hasSize(versionCount);
    }

    private Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version> firstOfVersionedListRoot(
            UserIDAuth owner) {
        try (Stream<Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version>> ls =
                     versionedDocusafeServices
                             .latestPrivate()
                             .listWithDetails(ListRequest.forDefaultPrivate(owner, "./"))
        ) {
            return ls.findFirst().orElseThrow(() -> new IllegalStateException("Empty directory"));
        }
    }
}
