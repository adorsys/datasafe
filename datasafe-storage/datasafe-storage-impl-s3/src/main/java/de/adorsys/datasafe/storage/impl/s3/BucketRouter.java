package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

/**
 * Deduces bucket name from resource location.
 */
@FunctionalInterface
public interface BucketRouter {

    String bucketName(AbsoluteLocation resource);
}
