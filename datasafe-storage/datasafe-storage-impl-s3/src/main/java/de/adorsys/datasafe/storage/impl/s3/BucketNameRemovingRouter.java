package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import lombok.RequiredArgsConstructor;

/**
 * Removes bucket name from resource key.
 */
@RequiredArgsConstructor
public class BucketNameRemovingRouter implements BucketRouter {

    private final String bucketName;

    @Override
    public String bucketName(AbsoluteLocation resource) {
        return bucketName;
    }

    @Override
    public String resourceKey(AbsoluteLocation resource) {
        String path = resource.location().getRawPath();
        int start = path.indexOf(bucketName);
        return path.substring(start + bucketName.length(), path.length()).replaceFirst("^/", "");
    }
}
