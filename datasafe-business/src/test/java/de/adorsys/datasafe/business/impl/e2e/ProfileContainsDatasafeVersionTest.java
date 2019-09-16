package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileContainsDatasafeVersionTest extends BaseE2ETest {

    @Test
    @SneakyThrows
    void getProfileVersionTest() {
        StorageDescriptor fs = fs();
        init(fs);
        UserIDAuth bob = registerUser("bob");
        String version = profileRetrievalService.privateProfile(bob).getVersion();
        assertThat(version).isNotEmpty();
    }

    private void init(WithStorageProvider.StorageDescriptor descriptor) {
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }
}
