package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.storage.api.actions.*;

public interface StorageService extends
        StorageListService,
        StorageReadService,
        StorageWriteService,
        StorageRemoveService,
        StorageCheckService {
}
