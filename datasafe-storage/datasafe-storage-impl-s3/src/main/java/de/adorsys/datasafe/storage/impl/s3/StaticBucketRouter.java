package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import lombok.RequiredArgsConstructor;

import java.util.function.UnaryOperator;

@RequiredArgsConstructor
public class StaticBucketRouter implements BucketRouter {

    private final String region;
    private final String bucketName;

    @Override
    public String bucketName(AbsoluteLocation resource) {
        return bucketName;
    }

    @Override
    public String resourceKey(AbsoluteLocation resource) {
        UnaryOperator<String> trimStartingSlash = str -> str.replaceFirst("^/", "");

        String resourcePath = trimStartingSlash.apply(resource.location().getRawPath());
        String bucketNameWithRegion = region + "/" + bucketName;
        if (bucketName == null || "".equals(bucketName) || !resourcePath.startsWith(bucketNameWithRegion)) {
            return resourcePath;
        }

        return trimStartingSlash.apply(resourcePath.substring(resourcePath.indexOf(bucketNameWithRegion) + bucketNameWithRegion.length()));
    }
}
