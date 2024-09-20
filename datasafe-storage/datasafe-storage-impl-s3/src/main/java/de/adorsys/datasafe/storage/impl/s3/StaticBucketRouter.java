package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import lombok.RequiredArgsConstructor;

import java.util.function.UnaryOperator;

@RequiredArgsConstructor
public class StaticBucketRouter implements BucketRouter {

    private final String bucketName;

    @Override
    public String bucketName(AbsoluteLocation resource) {
        return bucketName;
    }

    @Override
    public String resourceKey(AbsoluteLocation resource) {
        UnaryOperator<String> trimStartingSlash = str -> str.replaceFirst("^/", "");

        String resourcePath = trimStartingSlash.apply(resource.location().getRawPath());
        if (resourcePath.startsWith("eu-central-1/")) {
            resourcePath = resourcePath.substring("eu-central-1/".length());
        }

        if (bucketName == null || "".equals(bucketName) || !resourcePath.startsWith(bucketName)) {
            return resourcePath;
        }

        return trimStartingSlash.apply(resourcePath.substring(bucketName.length()));
    }
}
