package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This storage delegates real work of reading/writing/listing files to actual storage implementation
 * based on {@link URI#getScheme()}.
 * So that, for example:
 * s3://my-bucket/folder/file.txt will read file from S3 StorageService
 * file:///users/folder/file.txt will read file from local filesystem
 */
public class SchemeDelegatingStorage implements StorageService {

    private final Map<String, StorageService> storageByScheme;

    /**
     * Registers declared storage providers
     * @param storageByProtocol Storage operation handler by protocol.
     */
    public SchemeDelegatingStorage(Map<String, StorageService> storageByProtocol) {
        this.storageByScheme = storageByProtocol;
    }

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
    public OutputStream write(AbsoluteLocation location) {
        return service(location).write(location);
    }

    private StorageService service(AbsoluteLocation location) {
        String protocol = location.location().asURI().getScheme();
        StorageService service = storageByScheme.get(protocol);
        if (null == service) {
            throw new IllegalArgumentException("No storage service for this protocol: " + protocol);
        }

        return service;
    }
}
