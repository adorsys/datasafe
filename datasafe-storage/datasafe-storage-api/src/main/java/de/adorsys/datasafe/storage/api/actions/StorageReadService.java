package de.adorsys.datasafe.storage.api.actions;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

import java.io.InputStream;

/**
 * Raw file read operation at a given location.
 */
@FunctionalInterface
public interface StorageReadService {

    /**
     * @param location absolute bucket path with access credentials
     * @return data stream of resource to read from
     */
    InputStream read(AbsoluteLocation location);
}
