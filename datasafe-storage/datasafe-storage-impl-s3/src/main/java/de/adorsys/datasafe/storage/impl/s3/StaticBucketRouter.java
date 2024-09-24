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

        if (bucketName != null && !bucketName.isEmpty()) {
            if (resourcePath.startsWith(bucketName)) {
                return trimStartingSlash.apply(resourcePath.substring(resourcePath.indexOf(bucketName) + bucketName.length()));
            }
            String bucketNameWithRegion = region + "/" + bucketName;
            if (resourcePath.startsWith(bucketNameWithRegion)) {
                return trimStartingSlash.apply(resourcePath.substring(resourcePath.indexOf(bucketNameWithRegion) + bucketNameWithRegion.length()));
            }
        }
        return resourcePath;
    }
}
