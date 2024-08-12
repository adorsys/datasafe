package de.adorsys.datasafe.rest.impl.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import de.adorsys.datasafe.storage.impl.s3.S3ClientFactory;

import java.net.URI;

public class BasicS3Factory implements S3Factory {

    @Override
    public S3Client getClient(String endpointUrl, String region, String accessKey, String secretKey) {
        return S3ClientFactory.getClient(endpointUrl, region, accessKey, secretKey);
    }
    @Override
    public S3Client getAmazonClient(String region, String accessKey, String secretKey) {
        return S3ClientFactory.getAmazonClient(region, accessKey, secretKey);
    }
}