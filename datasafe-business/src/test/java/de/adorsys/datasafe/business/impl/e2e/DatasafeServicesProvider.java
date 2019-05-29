package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatasafeServicesProvider {

    public static DefaultDatasafeServices defaultDatasafeServices(StorageService storageService, Uri systemRoot) {
        return DaggerDefaultDatasafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return "PAZZWORD";
                    }

                    @Override
                    public Uri systemRoot() {
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

    public static VersionedDatasafeServices versionedDatasafeServices(StorageService storageService, Uri systemRoot) {
        return DaggerVersionedDatasafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return "PAZZWORD";
                    }

                    @Override
                    public Uri systemRoot() {
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
