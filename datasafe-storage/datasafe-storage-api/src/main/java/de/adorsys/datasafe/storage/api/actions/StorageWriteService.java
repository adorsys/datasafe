package de.adorsys.datasafe.storage.api.actions;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

import java.io.OutputStream;

/**
 * Raw file write operation at a given location.
 */
@FunctionalInterface
public interface StorageWriteService {

    /**
     * @param location absolute bucket path with credentials (if necessary)
     * @return data stream of resource to write to
     * @apiNote Resulting stream should be closed properly
     */
    OutputStream write(AbsoluteLocation location);
}


