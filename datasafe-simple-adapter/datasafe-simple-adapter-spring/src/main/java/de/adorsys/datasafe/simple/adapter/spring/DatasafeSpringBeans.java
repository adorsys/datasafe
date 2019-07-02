package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import de.adorsys.datasafe.simple.adapter.impl.SimpleDatasafeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.FileSystems;

@Configuration
@Slf4j
public class DatasafeSpringBeans {
    public DatasafeSpringBeans() {
        log.info("INIT");
    }

    @Bean
    SpringSimpleDatasafeServiceFactory simpleDatasafeServiceFactory(SpringDFSCredentialProperties springDFSCredentialProperties) {
        return new SpringSimpleDatasafeServiceFactory(getDFSCredentialsFromSpringDFSCredentialProperties(springDFSCredentialProperties));
    }

    private DFSCredentials getDFSCredentialsFromSpringDFSCredentialProperties(SpringDFSCredentialProperties properties) {
        if (properties == null) {
            throw new SimpleAdapterException("Spring properties are not set. Please use at least one of those:\n" + SpringFilesystemDFSCredentialsProperties.template + SpringAmazonS3DFSCredentialsProperties.template);
        }
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
            throw new SimpleAdapterException("missing switch for SpringDFSCredentialProperties");
        }

        return dfsCredentials;
    }

    @Bean
    public SimpleDatasafeService simpleDatasafeService(SpringSimpleDatasafeServiceFactory factory) {
        return factory.getSimpleDataSafeServiceWithSubdir("");

    }
}
