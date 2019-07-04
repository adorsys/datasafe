package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
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
