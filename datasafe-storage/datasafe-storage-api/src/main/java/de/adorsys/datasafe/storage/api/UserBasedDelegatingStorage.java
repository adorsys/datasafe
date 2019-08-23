package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserBasedDelegatingStorage extends BaseDelegatingStorage {
    private final Map<String, StorageService> clientByBucket = new ConcurrentHashMap<>();
    private final List<String> amazonBuckets;

    // Builder to create S3 or other kind of Storage service
    private final Function<String, StorageService> storageServiceBuilder;

    public UserBasedDelegatingStorage(Function<String, StorageService> storageServiceBuilder, List<String> amazonBuckets) {
        this.storageServiceBuilder = storageServiceBuilder;
        this.amazonBuckets = amazonBuckets;
    }

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
        // example location: s3://datasafe-test1/073047da-dd68-4f70-b9bf-5759d7e30c85/users/user-8/private/files/
        //                  s3://datasafe-test1/073047da-dd68-4f70-b9bf-5759d7e30c85/profiles/private/user-3/
        Pattern userPattern = Pattern.compile(".+/user-(\\d+).*");
        Matcher matcher = userPattern.matcher(location.location().asString());
        matcher.matches();
        int userNum = Integer.parseInt(matcher.group(1));
        return amazonBuckets.get(userNum % amazonBuckets.size());
    }
}
