package de.adorsys.datasafe.rest.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.storage.S3StorageService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.security.Security;

@Configuration
public class DatasafeConfig {

    private final DatasafeProperties properties;

    private final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .enablePathStyleAccess()
            .build();

    @Autowired
    public DatasafeConfig(DatasafeProperties properties) {
        this.properties = properties;
    }

    @Bean
    StorageService storageService() {
        return new S3StorageService(s3, properties.getBucketName());
    }

    @Bean
    DefaultDatasafeServices datasafeService() {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerDefaultDatasafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return properties.getKeystorePassword();
                    }

                    @Override
                    public URI systemRoot() {
                        return URI.create(properties.getSystemRoot());
                    }
                })
                .storageList(new S3StorageService(s3, properties.getBucketName()))
                .storageRead(new S3StorageService(s3, properties.getBucketName()))
                .storageWrite(new S3StorageService(s3, properties.getBucketName()))
                .storageRemove(new S3StorageService(s3, properties.getBucketName()))
                .storageCheck(new S3StorageService(s3, properties.getBucketName()))
                .build();
    }
}
