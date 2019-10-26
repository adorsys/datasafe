package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.spring.factory.SpringSimpleDatasafeServiceFactory;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringAmazonS3DFSCredentialsProperties;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringDFSCredentialProperties;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringDatasafeEncryptionProperties;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringFilesystemDFSCredentialsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        SpringDFSCredentialProperties.class,
        SpringFilesystemDFSCredentialsProperties.class,
        SpringAmazonS3DFSCredentialsProperties.class,
        SpringDatasafeEncryptionProperties.class
})
@Slf4j
public class DatasafeSpringBeans {

    public DatasafeSpringBeans() {
        log.info("INIT of DatasafeSpringBeans");
    }

    @Bean
    SpringSimpleDatasafeServiceFactory simpleDatasafeServiceFactory() {
        return new SpringSimpleDatasafeServiceFactory();
    }

    @Bean
    DFSCredentials dfsCredentials(SpringDFSCredentialProperties properties) {
        return SpringPropertiesToDFSCredentialsUtil.dfsCredentials(properties);
    }

    @Bean
    public SimpleDatasafeService simpleDatasafeService(SpringSimpleDatasafeServiceFactory factory) {
        return factory.getSimpleDataSafeServiceWithSubdir("");

    }
}
