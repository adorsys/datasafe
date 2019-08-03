package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

/**
 * Deduces bucket name from resource location and resource key.
 */
public interface BucketRouter {

    String bucketName(AbsoluteLocation resource);
    String resourceKey(AbsoluteLocation resource);
}
