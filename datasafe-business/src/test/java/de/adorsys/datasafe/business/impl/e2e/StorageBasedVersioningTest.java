package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.callback.PhysicalVersionCallback;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class StorageBasedVersioningTest extends BaseE2ETest {

    @Test
    @SneakyThrows
    void testVersionedWriteReturnsVersionId() {
        init(cephVersioned());

        registerJohnAndJane();

        AtomicReference<String> version = new AtomicReference<>();
        try (OutputStream os = writeToPrivate.write(WriteRequest.forDefaultPrivate(john, "file.txt")
                .toBuilder()
                .callback((PhysicalVersionCallback) version::set)
                .build())
        ) {
            os.write("Hello".getBytes());
        }

        assertThat(version.get()).isNotBlank();
    }

    private void init(WithStorageProvider.StorageDescriptor descriptor) {
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }
}
