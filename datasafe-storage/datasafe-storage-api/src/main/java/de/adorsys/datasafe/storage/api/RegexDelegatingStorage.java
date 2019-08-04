package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * This storage delegates real work of reading/writing/listing files to actual storage implementation
 * based on regex matching of received URI.
 * So that, for example:
 * s3://my-bucket/folder/file.txt will read file from S3 StorageService
 * file:///users/folder/file.txt will read file from local filesystem
 */
public class RegexDelegatingStorage extends BaseDelegatingStorage {

    private final Map<Pattern, StorageService> storageByPattern;

    /**
     * Registers declared storage providers
     * @param storageByPattern Storage operation handler by its matching regex.
     */
    public RegexDelegatingStorage(Map<Pattern, StorageService> storageByPattern) {
        this.storageByPattern = storageByPattern;
    }

    @Override
    protected StorageService service(AbsoluteLocation location) {
        String uri = location.location().asString();
        return storageByPattern.entrySet()
                .stream()
                .filter(it -> it.getKey().matcher(uri).matches())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Storage backing URI " + uri + " not found"));
    }
}
