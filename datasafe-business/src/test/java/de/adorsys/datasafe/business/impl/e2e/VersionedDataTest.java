package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDocusafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDocusafeServices;
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

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class VersionedDataTest extends WithStorageProvider {

    private static final String MESSAGE_ONE = "Hello here 1";
    private static final String MESSAGE_TWO = "Hello here 2";
    private static final String MESSAGE_THREE = "Hello here 3";

    private static final String FOLDER = "folder1";
    private static final String PRIVATE_FILE = "secret.txt";
    private static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;

    private VersionedDocusafeServices versionedDocusafeServices;

    @ParameterizedTest
    @MethodSource("storages")
    void testVersionedWriteTopLevel(WithStorageProvider.StorageDescriptor descriptor) {
        prepareTestAndDoWritesWithDiffMessageInSameLocation(descriptor);

        String readingResult = readPrivateUsingPrivateKey(jane, DefaultPrivateResource.forPrivate(PRIVATE_FILE_PATH));

        assertThat(readingResult).isEqualTo(MESSAGE_THREE);
    }

    @ParameterizedTest
    @MethodSource("storages")
    void testVersionedWriteUsingDirectAccess(WithStorageProvider.StorageDescriptor descriptor) {
        prepareTestAndDoWritesWithDiffMessageInSameLocation(descriptor);

        AbsoluteResourceLocation<PrivateResource> latest = getFirstFileInPrivate(jane);
        String directResult = readRawPrivateUsingPrivateKey(jane, latest.getResource());

        assertThat(directResult).isEqualTo(MESSAGE_THREE);
        assertThat(latest.getResource().decryptedPath()).asString().contains(PRIVATE_FILE_PATH);
    }

    @Override
    @SneakyThrows
    protected void writeDataToPrivate(UserIDAuth auth, String path, String data) {
        try (OutputStream stream =
                     versionedDocusafeServices.versionedPrivate().write(WriteRequest.forDefaultPrivate(auth, path))) {
            stream.write(data.getBytes());
        }
    }

    @Override
    @SneakyThrows
    protected String readPrivateUsingPrivateKey(UserIDAuth user, PrivateResource location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream dataStream =
                     versionedDocusafeServices.versionedPrivate().read(ReadRequest.forPrivate(user, location))) {
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
    protected AbsoluteResourceLocation<PrivateResource> getFirstFileInPrivate(UserIDAuth inboxOwner) {
        List<AbsoluteResourceLocation<PrivateResource>> files = versionedDocusafeServices.versionedPrivate().list(
                ListRequest.forDefaultPrivate(inboxOwner, "./")
        ).collect(Collectors.toList());

        log.info("{} has {} in PRIVATE", inboxOwner.getUserID().getValue(), files);
        return files.get(0);
    }

    private VersionedDocusafeServices buildVersioned(WithStorageProvider.StorageDescriptor descriptor) {
        return DaggerVersionedDocusafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return "PAZZWORD";
                    }

                    @Override
                    public URI systemRoot() {
                        return descriptor.getLocation();
                    }
                })
                .storageList(descriptor.getStorageService())
                .storageRead(descriptor.getStorageService())
                .storageWrite(descriptor.getStorageService())
                .storageRemove(descriptor.getStorageService())
                .storageCheck(descriptor.getStorageService())
                .build();
    }


    private void prepareTestAndDoWritesWithDiffMessageInSameLocation(StorageDescriptor descriptor) {
        this.versionedDocusafeServices = buildVersioned(descriptor);
        this.services = versionedDocusafeServices;

        registerJohnAndJane(descriptor.getLocation());

        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_ONE);
        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_TWO);
        writeDataToPrivate(jane, PRIVATE_FILE_PATH, MESSAGE_THREE);
    }
}
