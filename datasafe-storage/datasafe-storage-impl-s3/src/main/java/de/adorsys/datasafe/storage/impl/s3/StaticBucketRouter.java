package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StaticBucketRouter implements BucketRouter {

    private final String bucketName;

    @Override
    public String bucketName(AbsoluteLocation resource) {
        return bucketName;
    }
}
