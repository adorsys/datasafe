package de.adorsys.datasafe.business.api.storage;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;

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
    InputStream read(AbsoluteResourceLocation location);
}
