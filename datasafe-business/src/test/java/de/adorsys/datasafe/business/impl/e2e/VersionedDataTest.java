package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.RemoveRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.*;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.version.types.DFSVersion;
import de.adorsys.datasafe.shared.Position;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class VersionedDataTest extends WithStorageProvider {

    private static final String MESSAGE_ONE = "Hello here 1";
    private static final String MESSAGE_TWO = "Hello here 2";
    private static final String MESSAGE_THREE = "Hello here 3";

    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";
    private static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;

    private VersionedDatasafeServices versionedDocusafeServices;

    @ParameterizedTest
    @MethodSource("allStorages")
    void testVersionedWriteTopLevel(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation(descriptor);

        String readingResult = readPrivateUsingPrivateKey(jane, BasePrivateResource.forPrivate(PRIVATE_FILE_PATH));

        assertThat(readingResult).isEqualTo(MESSAGE_THREE);
        validateThereAreVersions(jane, 3);
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void testVersionedWriteUsingDirectAccess(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation(descriptor);

        AbsoluteLocation<ResolvedResource> latest = getFirstFileInPrivate(jane);
        String directResult = readRawPrivateUsingPrivateKey(jane, latest.getResource().asPrivate());

        assertThat(directResult).isEqualTo(MESSAGE_THREE);
        assertThat(latest.getResource().asPrivate().decryptedPath()).asString().contains(PRIVATE_FILE_PATH);
        validateThereAreVersions(jane, 3);
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void testVersionedRemove(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation(descriptor);

        Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version> first =
                Position.first(versionedListRoot(jane));
        versionedDocusafeServices.latestPrivate().remove(
                RemoveRequest.forPrivate(jane, first.stripVersion().asPrivate())
        );

        assertThat(listRoot(jane)).isEmpty();
        validateThereAreVersions(jane, 3);
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void testVersionsOf(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation(descriptor);

        List<Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion>> versionedResource =
                versionedDocusafeServices.versionInfo()
                        .versionsOf(ListRequest.forDefaultPrivate(jane, "./"))
                        .collect(Collectors.toList());

        assertThat(versionedResource).hasSize(3);
        assertThat(versionedResource)
                .extracting(Versioned::stripVersion)
                .extracting(ResourceLocation::location)
                .extracting(URI::toString)
                .containsExactly(PRIVATE_FILE_PATH, PRIVATE_FILE_PATH, PRIVATE_FILE_PATH);
    }

    // this test imitates removal of old file versions
    @ParameterizedTest
    @MethodSource("allStorages")
    void testOldRemoval(WithStorageProvider.StorageDescriptor descriptor) {
        init(descriptor);

        registerAndDoWritesWithDiffMessageInSameLocation(descriptor);

        List<Versioned<AbsoluteLocation<ResolvedResource>, ResolvedResource, DFSVersion>> versionedResource =
                versionedDocusafeServices.versionInfo()
                        .listJoinedWithLatest(ListRequest.forDefaultPrivate(jane, "./"))
                        .collect(Collectors.toList());

        // remove `old` versions
        List<ResolvedResource> toRemove = versionedResource.stream()
                .filter(it -> !it.stripVersion().location().equals(it.absolute().location()))
                // ideally you want to filter out by time as well - i.e. older than 1 hour
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

        initialize(datasafeServices);
        this.versionedDocusafeServices = datasafeServices;
    }


    private void registerAndDoWritesWithDiffMessageInSameLocation(StorageDescriptor descriptor) {
        registerJohnAndJane(descriptor.getLocation());

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);
        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_TWO);
        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_THREE);
    }

    private void validateThereAreVersions(UserIDAuth user, int versionCount) {
        assertThat(
                versionedDocusafeServices.privateService().list(ListRequest.forDefaultPrivate(user, "./"))
        ).hasSize(versionCount);
    }
}
