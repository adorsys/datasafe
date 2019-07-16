package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.security.Security;
import java.util.concurrent.Executors;

/**
 * Configures default (non-versioned) Datasafe service that uses S3 client as storage provider.
 * Encryption provider: BouncyCastle.
 */
@Slf4j
@Configuration
public class DatasafeConfig {
    public static final String FILESYSTEM_ENV = "USE_FILESYSTEM";

    @ConditionalOnProperty(FILESYSTEM_ENV)
    @Bean
    StorageService fsStorageService(DatasafeProperties properties) {
        String root = System.getenv(FILESYSTEM_ENV);
        log.info("==================== FILESYSTEM");
        log.info("build DFS to FILESYSTEM with root " + root);
        properties.setSystemRoot(root);
        return new FileSystemStorageService(Paths.get(root));
    }

    @ConditionalOnMissingBean(StorageService.class)
    @Bean
    StorageService s3StorageService(DatasafeProperties properties) {
        log.info("==================== AMAZONS3");
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .enablePathStyleAccess()
                .build();


        return new S3StorageService(
                s3,
                properties.getBucketName(),
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        );
    }


    @Bean
    DFSConfig dfsConfig(DatasafeProperties properties) {
        return new DefaultDFSConfig(properties.getSystemRoot(), properties.getKeystorePassword());
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
