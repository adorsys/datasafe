package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.security.Security;

@Configuration
public class DatasafeConfig {

    private final DatasafeProperties properties;
    private final AmazonS3 s3;

    @Autowired
    public DatasafeConfig(DatasafeProperties properties, AmazonS3 s3) {
        this.properties = properties;
        this.s3 = s3;
    }

    @Bean
    StorageService storageService() {
        return new S3StorageService(s3, properties.getBucketName());
    }

    @Bean
    DefaultDatasafeServices datasafeService(StorageService storageService) {

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
                .storageList(storageService)
                .storageRead(storageService)
                .storageWrite(storageService)
                .storageRemove(storageService)
                .storageCheck(storageService)
                .build();
    }
}
