package de.adorsys.datasafe.business.api.storage;

import de.adorsys.datasafe.business.api.storage.actions.StorageListService;
import de.adorsys.datasafe.business.api.storage.actions.StorageReadService;
import de.adorsys.datasafe.business.api.storage.actions.StorageRemoveService;
import de.adorsys.datasafe.business.api.storage.actions.StorageWriteService;

public interface StorageService extends
        StorageListService,
        StorageReadService,
        StorageWriteService,
        StorageRemoveService {
}
