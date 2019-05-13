package de.adorsys.datasafe.business.api.storage;

import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;

/**
 * Remove operation at a given location with all subdirs.
 */
@FunctionalInterface
public interface StorageRemoveService {

    /**
     * Removes bucket contents.
     * @param location absolute bucket path, with credentials, which should be removed
     */
    void remove(AbsoluteResourceLocation location);
}
