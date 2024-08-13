package de.adorsys.datasafe.rest.impl.config;

import software.amazon.awssdk.services.s3.S3Client;

public interface S3Factory {

    S3Client getClient(String endpointUrl, String region, String accessKey, String secretKey);
    S3Client getAmazonClient(String region, String accessKey, String secretKey);
}
