package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.storage.api.actions.StorageCheckService;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;

/**
 * Groups all operations on data storage into a single class.
 * Note that it is expected that all operations return and use URI-safe values, so that i.e.:
 * - List for `path+/path` list will return `path%20/path/somefile` wrapped in URI
 * - Write for `path+/file` will write to `path%20/file`
 * - Read for `path+/file` will read from `path%20/file`
 */
public interface StorageService extends
        StorageListService,
        StorageReadService,
        StorageWriteService,
        StorageRemoveService,
        StorageCheckService {
}
