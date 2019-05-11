package de.adorsys.datasafe.business.api.storage;

import de.adorsys.datasafe.business.api.storage.actions.*;

public interface StorageService extends
        StorageListService,
        StorageReadService,
        StorageWriteService,
        StorageRemoveService,
        StorageCheckService {
}
