package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.services.s3.AmazonS3;

public interface S3Factory {

    AmazonS3 getClient(String endpointUrl, String region, String accessKey, String secretKey);
    AmazonS3 getAmazonClient(String region, String accessKey, String secretKey);
}
