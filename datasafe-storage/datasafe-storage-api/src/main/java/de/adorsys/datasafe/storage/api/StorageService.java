package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.storage.api.actions.*;

/**
 * Groups all operations on data storage into a single class.
 */
public interface StorageService extends
        StorageListService,
        StorageReadService,
        StorageWriteService,
        StorageRemoveService,
        StorageCheckService {
}
