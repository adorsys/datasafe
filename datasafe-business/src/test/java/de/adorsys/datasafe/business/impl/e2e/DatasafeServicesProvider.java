package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.experimental.UtilityClass;

/**
 * Factory to get Datasafe services.
 */
@UtilityClass
public class DatasafeServicesProvider {

    public static final String STORE_PAZZWORD = "PAZZWORD";

    public static DefaultDatasafeServices defaultDatasafeServices(StorageService storageService, Uri systemRoot) {
        return DaggerDefaultDatasafeServices
                .builder()
                .config(dfsConfig(systemRoot))
                .storage(storageService)
                .build();
    }

    public static VersionedDatasafeServices versionedDatasafeServices(StorageService storageService, Uri systemRoot) {
        return DaggerVersionedDatasafeServices
                .builder()
                .config(dfsConfig(systemRoot))
                .storage(storageService)
                .build();
    }

    public DFSConfig dfsConfig(Uri systemRoot) {
        return new DefaultDFSConfig(systemRoot, STORE_PAZZWORD);
    }
}
