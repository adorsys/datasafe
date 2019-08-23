package de.adorsys.datasafe.storage.api.actions;

import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.WithCallback;

import java.io.OutputStream;
import java.util.Optional;

/**
 * Raw file write operation at a given location. Paths use URL-encoding.
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

    /**
     * For some storages that cache data before writing it (i.e. {@code S3StorageService}) this should indicate
     * buffer size, so that callers can optimize some parts of their logic.
     * @param location resource to check for buffer size
     * @return Buffer size in bytes, or {@code -1} if undefined
     */
    default Optional<Integer> flushChunkSize(AbsoluteLocation location) {
        return Optional.empty();
    }
}


