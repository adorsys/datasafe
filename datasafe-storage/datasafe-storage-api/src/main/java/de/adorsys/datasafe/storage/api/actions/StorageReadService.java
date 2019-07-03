package de.adorsys.datasafe.storage.api.actions;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

import java.io.InputStream;

/**
 * Raw file read operation at a given location. Paths use URL-encoding.
 */
@FunctionalInterface
public interface StorageReadService {

    /**
     * @param location absolute bucket path with credentials (if necessary)
     * @return data stream of resource to read from
     * @apiNote Resulting stream should be closed properly
     */
    InputStream read(AbsoluteLocation location);
}
