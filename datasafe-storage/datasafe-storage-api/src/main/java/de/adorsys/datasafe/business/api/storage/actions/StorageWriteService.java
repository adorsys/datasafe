package de.adorsys.datasafe.business.api.storage.actions;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;

import java.io.OutputStream;

/**
 * Raw file write operation at a given location.
 */
@FunctionalInterface
public interface StorageWriteService {

    /**
     * @param location absolute bucket path with access credentials
     * @return data stream of resource to write to
     */
    OutputStream write(AbsoluteLocation location);
}


