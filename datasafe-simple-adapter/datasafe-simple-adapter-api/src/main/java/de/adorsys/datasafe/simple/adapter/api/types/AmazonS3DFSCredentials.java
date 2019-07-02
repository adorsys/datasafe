package de.adorsys.datasafe.simple.adapter.api.types;

import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AmazonS3DFSCredentials extends DFSCredentials {
    private final String rootBucket;
    private final String url;
    private final String secretKey;
    private final String accessKey;
    private final String region;

    public String getContainer() {
        return rootBucket.split("/")[0];
    }

}
