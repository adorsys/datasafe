package de.adorsys.datasafe.storage.api.actions;

import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.WithCallback;

import java.io.OutputStream;

/**
 * Raw file write operation at a given location.
 */
@FunctionalInterface
public interface StorageWriteService {

    /**
     * @param locationWithCallback absolute bucket path with credentials (if necessary) plus callbacks to be executed
     * when file is written, i.e. notify callee that we assigned some file version
     * @return data stream of resource to write to
     * @apiNote Resulting stream should be closed properly
     */
    OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback);
}


