package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.StorageVersion;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.callback.PhysicalVersionCallback;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.io.ByteStreams;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class StorageBasedVersioningTest extends BaseE2ETest {

    private static final String FILE = "file.txt";

    @Test
    void testVersionedWriteReturnsVersionId() {
        init(cephVersioned());
        registerJohnAndJane();

        String version = writeAndGetVersion(jane, FILE, "Hello");

        assertThat(version).isNotBlank();
    }

    @Test
    void testVersionedWriteSequenceAndThenReadLatest() {
        init(cephVersioned());
        registerJohnAndJane();

        writeAndGetVersion(jane, FILE, "Hello 1");
        writeAndGetVersion(jane, FILE, "Hello 2");
        writeAndGetVersion(jane, FILE, "Hello 3");

        assertThat(readPrivateUsingPrivateKey(jane, BasePrivateResource.forPrivate(FILE))).isEqualTo("Hello 3");
    }

    @Test
    void testVersionedWriteSequenceAndThenReadByVersion() {
        init(cephVersioned());
        registerJohnAndJane();

        String oldVersion = writeAndGetVersion(jane, FILE, "Hello 1");
        writeAndGetVersion(jane, FILE, "Hello 2");
        writeAndGetVersion(jane, FILE, "Hello 3");

        assertThat(readByVersion(jane, FILE, new StorageVersion(oldVersion))).isEqualTo("Hello 1");
    }

    @SneakyThrows
    private String writeAndGetVersion(UserIDAuth user, String path, String data) {
        AtomicReference<String> version = new AtomicReference<>();
        try (OutputStream os = writeToPrivate.write(WriteRequest.forDefaultPrivate(user, path)
                .toBuilder()
                .callback((PhysicalVersionCallback) version::set)
                .build())
        ) {
            os.write(data.getBytes());
        }

        return version.get();
    }

    @SneakyThrows
    private String readByVersion(UserIDAuth user, String path, StorageVersion version) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (InputStream is = readFromPrivate.read(ReadRequest.forDefaultPrivateWithVersion(user, path, version))
        ) {
            ByteStreams.copy(is, os);
        }

        return new String(os.toByteArray(), StandardCharsets.UTF_8);
    }

    private void init(WithStorageProvider.StorageDescriptor descriptor) {
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }
}
