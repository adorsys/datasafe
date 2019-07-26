package de.adorsys.datasafe.examples.business.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
class S3ClientFactory {

    AmazonS3 getClient(String endpointUrl, String accessKey, String secretKey) {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpointUrl, "eu-central-1")
                )
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(accessKey, secretKey)
                        )
                )
                .enablePathStyleAccess()
                .build();
    }
}
