package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.resource.Uri;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;
import java.util.concurrent.Executors;

/**
 * Configures default (non-versioned) Datasafe service that uses S3 client as storage provider.
 * Encryption provider: BouncyCastle.
 */
@Configuration
public class DatasafeConfig {

    private final DatasafeProperties properties;
    private final AmazonS3 s3;

    @Autowired
    public DatasafeConfig(DatasafeProperties properties, AmazonS3 s3) {
        this.properties = properties;
        this.s3 = s3;
    }

    /**
     * @return S3 based storage service
     */
    @Bean
    StorageService storageService() {
        return new S3StorageService(
                s3,
                properties.getBucketName(),
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        );
    }

    /**
     * @return Default implementation of Datasafe services.
     */
    @Bean
    DefaultDatasafeServices datasafeService(StorageService storageService) {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig(new Uri(properties.getSystemRoot()), properties.getKeystorePassword()))
                .storage(storageService)
                .build();
    }
}
