package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

import java.net.URI;
import java.util.Map;

/**
 * This storage delegates real work of reading/writing/listing files to actual storage implementation
 * based on {@link URI#getScheme()}.
 * So that, for example:
 * s3://my-bucket/folder/file.txt will read file from S3 StorageService
 * file:///users/folder/file.txt will read file from local filesystem
 */
public class SchemeDelegatingStorage extends BaseDelegatingStorage {

    private final Map<String, StorageService> storageByScheme;

    /**
     * Registers declared storage providers
     * @param storageByProtocol Storage operation handler by protocol.
     */
    public SchemeDelegatingStorage(Map<String, StorageService> storageByProtocol) {
        this.storageByScheme = storageByProtocol;
    }

    @Override
    protected StorageService service(AbsoluteLocation location) {
        String protocol = location.location().asURI().getScheme();
        StorageService service = storageByScheme.get(protocol);
        if (null == service) {
            throw new IllegalArgumentException("No storage service for this protocol: " + protocol);
        }

        return service;
    }
}
