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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.nio.file.FileSystems;
import java.security.Security;
import java.util.concurrent.Executors;

/**
 * Configures default (non-versioned) Datasafe service that uses S3 client as storage provider.
 * Encryption provider: BouncyCastle.
 */
@Slf4j
@Configuration
public class DatasafeConfig {
    public final static String FILESYSTEM_ENV = "USE_FILESYSTEM";
    public final static String AWS_BUCKET = "AWS_BUCKET";
    public final static String S3_PREFIX = "s3://";

    /**
     * @return storage service
     */
    @Bean
    StorageService storageService(DatasafeProperties properties) {

        String value = System.getenv(FILESYSTEM_ENV);
        if (value != null) {
            log.info("==================== FILESYSTEM");
            String[] parts = value.split(",");
            if (parts.length != 1) {
                throw new RuntimeException("expected <rootbucket> for " + FILESYSTEM_ENV);
            }
            String root = parts[0];

            log.info("build DFS to FILESYSTEM with root " + root);
            // return new FileSystemStorageService(FileSystems.getDefault().getPath(root));
            return new FileSystemStorageService(FileSystems.getDefault().getPath(root));
        }

        return getS3StorageService(properties);
    }

    @Bean
    DFSConfig dfsConfig(DatasafeProperties properties) {
        String value = System.getenv(FILESYSTEM_ENV);
        if (value != null) {
            log.info("init DFSConfig with filesystem");
            String[] parts = value.split(",");
            if (parts.length != 1) {
                throw new RuntimeException("expected <rootbucket> for " + FILESYSTEM_ENV);
            }
            String root = parts[0];
            URI systemRoot = FileSystems.getDefault().getPath(root).toAbsolutePath().toUri();
            return new DefaultDFSConfig(systemRoot, properties.getKeystorePassword());
        }
        String root = System.getenv(AWS_BUCKET);
        if (root != null) {
            log.info("init DFSConfig with amazons3");
            String systemRoot = S3_PREFIX + root;
            return new DefaultDFSConfig(systemRoot, properties.getKeystorePassword());
        }
        throw new RuntimeException("dont know how to create DFSConfig");



    }

    public S3StorageService getS3StorageService(DatasafeProperties properties) {
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
