package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * This storage delegates real work of reading/writing/listing files to actual storage implementation.
 */
public abstract class BaseDelegatingStorage implements StorageService {

    @Override
    public boolean objectExists(AbsoluteLocation location) {
        return service(location).objectExists(location);
    }

    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        return service(location).list(location);
    }

    @Override
    public InputStream read(AbsoluteLocation location) {
        return service(location).read(location);
    }

    @Override
    public void remove(AbsoluteLocation location) {
        service(location).remove(location);
    }

    @Override
    public OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback) {
        return service(locationWithCallback.getWrapped()).write(locationWithCallback);
    }

    protected abstract StorageService service(AbsoluteLocation location);
}
