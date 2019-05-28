package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonConfig {

    @Bean
    AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .enablePathStyleAccess()
                .build();
    }
}
