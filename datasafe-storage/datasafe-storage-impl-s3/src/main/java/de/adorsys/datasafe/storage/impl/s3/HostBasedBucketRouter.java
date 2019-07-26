package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

public class HostBasedBucketRouter implements BucketRouter {

    @Override
    public String bucketName(AbsoluteLocation resource) {
        return resource.location().asURI().getHost();
    }
}
