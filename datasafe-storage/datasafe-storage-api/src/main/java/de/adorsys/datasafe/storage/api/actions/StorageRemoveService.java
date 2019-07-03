package de.adorsys.datasafe.storage.api.actions;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

/**
 * Remove operation at a given location with all subdirs. Paths use URL-encoding.
 */
@FunctionalInterface
public interface StorageRemoveService {

    /**
     * Removes bucket contents.
     * @param location absolute bucket path, with credentials (if necessary), which should be removed
     */
    void remove(AbsoluteLocation location);
}
