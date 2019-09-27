package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.storage.impl.s3.S3ClientFactory;

public class BasicS3Factory implements S3Factory {

    @Override
    public AmazonS3 getClient(String endpointUrl, String region, String accessKey, String secretKey) {
        return S3ClientFactory.getClient(endpointUrl, region, accessKey, secretKey);
    }

    @Override
    public AmazonS3 getAmazonClient(String region, String accessKey, String secretKey) {
        return S3ClientFactory.getAmazonClient(region, accessKey, secretKey);
    }
}
