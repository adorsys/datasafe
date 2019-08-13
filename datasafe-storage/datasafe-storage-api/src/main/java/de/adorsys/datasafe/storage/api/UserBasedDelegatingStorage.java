package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.Uri;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class UserBasedDelegatingStorage extends BaseDelegatingStorage {
    private final Map<String, StorageService> clientByBucket = new ConcurrentHashMap<>();

    public UserBasedDelegatingStorage(Function<String, StorageService> storageServiceBuilder) {
        this.storageServiceBuilder = storageServiceBuilder;
    }

    // Builder to create S3 or other kind of Storage service
    private final Function<String, StorageService> storageServiceBuilder;

    @Override
    protected StorageService service(AbsoluteLocation location) {
        Uri uri = location.location();
        String[] parts = uri.getPath().replaceAll("^/", "").split("/");
        String userName = "profiles".equals(parts[1]) ? parts[3] : parts[2];
        String userNumber = userName.split("-")[1];
        int userNum = Integer.parseInt(userNumber);
        String bucketName = "datasafe-test" + (userNum % 3 + 1);
        return clientByBucket.computeIfAbsent(bucketName, storageServiceBuilder);
    }
}
