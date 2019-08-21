package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.Uri;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class UserBasedDelegatingStorage extends BaseDelegatingStorage {
    private final Map<String, StorageService> clientByBucket = new ConcurrentHashMap<>();
    private final String amazonBucket;

    public UserBasedDelegatingStorage(Function<String, StorageService> storageServiceBuilder, String amazonBucket) {
        this.storageServiceBuilder = storageServiceBuilder;
        this.amazonBucket = amazonBucket;
    }

    // Builder to create S3 or other kind of Storage service
    private final Function<String, StorageService> storageServiceBuilder;

    @Override
    protected StorageService service(AbsoluteLocation location) {
        String bucketName = getBucketNameFromLocation(location);
        return clientByBucket.computeIfAbsent(bucketName, storageServiceBuilder);
    }

    @Override
    public Optional<Integer> flushChunkSize(AbsoluteLocation location) {
        String bucketName = getBucketNameFromLocation(location);
        return clientByBucket.computeIfAbsent(bucketName, storageServiceBuilder).flushChunkSize(location);
    }

    private String getBucketNameFromLocation(AbsoluteLocation location) {
        Uri uri = location.location();
        String[] parts = uri.getPath().replaceAll("^/", "").split("/");
        String userName = "profiles".equals(parts[1]) ? parts[3] : parts[2];
        String userNumber = userName.split("-")[1];
        int userNum = Integer.parseInt(userNumber);
        String[] buckets = amazonBucket.split(",");
        return buckets[userNum % buckets.length];
    }
}
