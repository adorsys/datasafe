package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.resource.Uri;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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

    @Bean
    DFSConfig dfsConfig(DatasafeProperties properties) {
        return new DefaultDFSConfig(new Uri(properties.getSystemRoot()), properties.getKeystorePassword());
    }

    /**
     * @return S3 based storage service
     */
    @Bean
    StorageService storageService(AmazonS3 s3, DatasafeProperties properties) {
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
    DefaultDatasafeServices datasafeService(StorageService storageService, DFSConfig dfsConfig) {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerDefaultDatasafeServices
                .builder()
                .config(dfsConfig)
                .storage(storageService)
                .build();
    }

    @Bean
    VersionedDatasafeServices versionedDatasafeServices(StorageService storageService, DFSConfig dfsConfig) {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerVersionedDatasafeServices
                .builder()
                .config(dfsConfig)
                .storage(storageService)
                .build();
    }
}
