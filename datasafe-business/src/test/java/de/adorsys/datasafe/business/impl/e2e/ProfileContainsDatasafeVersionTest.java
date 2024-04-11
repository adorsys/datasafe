package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.global.Version;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileContainsDatasafeVersionTest extends BaseE2ETest {

    @Test
    @SneakyThrows
    void getProfileVersionTest() {
        init();

        UserIDAuth bob = registerUser("bob");

        Version version = profileRetrievalService.privateProfile(bob).getAppVersion();
        assertThat(version.getId()).isEqualTo(Version.current().getId());
    }

    private void init() {
        StorageDescriptor descriptor = fs();
        DefaultDatasafeServices datasafeServices = DatasafeServicesProvider
                .defaultDatasafeServices(descriptor.getStorageService().get(), descriptor.getLocation());
        initialize(DatasafeServicesProvider.dfsConfig(descriptor.getLocation()), datasafeServices);
    }
}
