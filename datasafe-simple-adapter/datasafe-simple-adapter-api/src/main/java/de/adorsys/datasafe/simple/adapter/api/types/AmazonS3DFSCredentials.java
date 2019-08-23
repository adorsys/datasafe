package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.Builder;
import lombok.Getter;

// TODO: it is far more than credentials, rename it and parent class to DFSConfig in next major release
@Builder(toBuilder = true)
@Getter
public class AmazonS3DFSCredentials extends DFSCredentials {
    private final String rootBucket;
    private final String url;
    private final String secretKey;
    private final String accessKey;
    private final String region;

    @Builder.Default
    private final boolean noHttps = false;

    @Builder.Default
    private final int threadPoolSize = 5;

    @Builder.Default
    private final int queueSize = 5;

    public String getContainer() {
        return rootBucket.split("/")[0];
    }

}
