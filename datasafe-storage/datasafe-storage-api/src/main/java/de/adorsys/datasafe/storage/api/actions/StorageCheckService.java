package de.adorsys.datasafe.storage.api.actions;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

/**
 * Represents operation that checks if specified resource exists.
 */
@FunctionalInterface
public interface StorageCheckService {

    /**
     * @param location Resource location with credentials (if necessary)
     * @return Does the resource at {@code location} exists
     */
    boolean objectExists(AbsoluteLocation location);
}
