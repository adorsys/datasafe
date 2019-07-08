package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures S3 access using Amazon S3 java client, credentials and bucket are expected to be provided via
 * environment variables AWS_ACCESS_KEY and AWS_SECRET_KEY, bucket name - using AWS_BUCKET.
 */
@Configuration
public class AmazonConfig {

    /**
     * @return Amazon S3 client built based on credentials and parameters from environment.
     */
    @Bean
    AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .enablePathStyleAccess()
                .build();
    }
}
