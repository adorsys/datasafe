package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import de.adorsys.datasafe.simple.adapter.spring.factory.SpringSimpleDatasafeServiceFactory;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringAmazonS3DFSCredentialsProperties;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringDFSCredentialProperties;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringFilesystemDFSCredentialsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        SpringDFSCredentialProperties.class,
        SpringFilesystemDFSCredentialsProperties.class,
        SpringAmazonS3DFSCredentialsProperties.class}
)
@Slf4j
public class DatasafeSpringBeans {
    public DatasafeSpringBeans() {
        log.info("INIT of DatasafeSpringBeans");
    }

    @Bean
    SpringSimpleDatasafeServiceFactory simpleDatasafeServiceFactory(SpringDFSCredentialProperties properties) {
        if (properties == null) {
            throw new SimpleAdapterException("Spring properties are null. Please use at least one of those:\n" + SpringFilesystemDFSCredentialsProperties.template + SpringAmazonS3DFSCredentialsProperties.template);
        }
        log.info("*** properties comming from spring look like:" + properties);
        return new SpringSimpleDatasafeServiceFactory(getDFSCredentialsFromSpringDFSCredentialProperties(properties));
    }

    private DFSCredentials getDFSCredentialsFromSpringDFSCredentialProperties(SpringDFSCredentialProperties properties) {
        DFSCredentials dfsCredentials = null;
        if (properties.getAmazons3() != null) {
            SpringAmazonS3DFSCredentialsProperties props = properties.getAmazons3();
            dfsCredentials = AmazonS3DFSCredentials.builder()
                    .rootBucket(props.getRootbucket())
                    .accessKey(props.getAccesskey())
                    .secretKey(props.getSecretkey())
                    .region(props.getRegion())
                    .url(props.getUrl())
                    .build();
        }
        if (properties.getFilesystem() != null) {
            SpringFilesystemDFSCredentialsProperties props = properties.getFilesystem();
            dfsCredentials = FilesystemDFSCredentials.builder()
                    .root(props.getRootbucket())
                    .build();
        }
        if (dfsCredentials == null) {
            throw new SimpleAdapterException("Spring properties are not set correctly. Please use at least one of those:\n" + SpringFilesystemDFSCredentialsProperties.template + SpringAmazonS3DFSCredentialsProperties.template);
        }

        return dfsCredentials;
    }

    @Bean
    public SimpleDatasafeService simpleDatasafeService(SpringSimpleDatasafeServiceFactory factory) {
        return factory.getSimpleDataSafeServiceWithSubdir("");

    }
}
