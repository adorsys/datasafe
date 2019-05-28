package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import lombok.experimental.UtilityClass;

import java.net.URI;

@UtilityClass
public class DatasafeServicesProvider {

    public static DefaultDatasafeServices defaultDatasafeServices(StorageService storageService, URI systemRoot) {
        return DaggerDefaultDatasafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return "PAZZWORD";
                    }

                    @Override
                    public URI systemRoot() {
                        return systemRoot;
                    }
                })
                .storageList(storageService)
                .storageRead(storageService)
                .storageWrite(storageService)
                .storageRemove(storageService)
                .storageCheck(storageService)
                .build();
    }

    public static VersionedDatasafeServices versionedDatasafeServices(StorageService storageService, URI systemRoot) {
        return DaggerVersionedDatasafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return "PAZZWORD";
                    }

                    @Override
                    public URI systemRoot() {
                        return systemRoot;
                    }
                })
                .storageList(storageService)
                .storageRead(storageService)
                .storageWrite(storageService)
                .storageRemove(storageService)
                .storageCheck(storageService)
                .build();
    }
}
