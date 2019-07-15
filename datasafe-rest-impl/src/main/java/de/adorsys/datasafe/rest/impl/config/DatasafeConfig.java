package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.config.MultiDFSConfig;
import de.adorsys.datasafe.storage.api.SchemeDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.db.DatabaseConnectionRegistry;
import de.adorsys.datasafe.storage.impl.db.DatabaseCredentials;
import de.adorsys.datasafe.storage.impl.db.DatabaseStorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.resource.Uri;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Configures default (non-versioned) Datasafe service that uses S3 client as storage provider.
 * Encryption provider: BouncyCastle.
 */
@Configuration
public class DatasafeConfig {

    private static final Set<String> ALLOWED_TABLES = ImmutableSet.of("users", "private_profiles", "public_profiles");


    private DatasafeProperties datasafeProperties;

    @Inject
    DatasafeConfig(DatasafeProperties datasafeProperties) {
        this.datasafeProperties = datasafeProperties;
    }

    @Bean
    DFSConfig dfsConfig(DatasafeProperties properties) {
        return new DefaultDFSConfig(new Uri(properties.getFsDevPath()), properties.getKeystorePassword());
    }

    /**
     * @return S3 based storage service
     */
    @Bean
    StorageService storageService(AmazonS3 s3, DatasafeProperties properties) {
        /*return new S3StorageService(
                s3,
                properties.getBucketName(),
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        );*/
         return new FileSystemStorageService(Paths.get(properties.getFsDevPath()));

    }

    /**
     * @return Default implementation of Datasafe services.
     */
    @Bean
    @ConditionalOnProperty(name = "DATASAFE_SINGLE_STORAGE", havingValue="true")
    @Order(1)
    DefaultDatasafeServices datasafeService(StorageService storageService, DFSConfig dfsConfig) {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerDefaultDatasafeServices
                .builder()
                .config(dfsConfig)
                .storage(storageService)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "DATASAFE_SINGLE_STORAGE", havingValue="true")
    @Order(2)
    VersionedDatasafeServices versionedDatasafeServices(StorageService storageService, DFSConfig dfsConfig) {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerVersionedDatasafeServices
                .builder()
                .config(dfsConfig)
                .storage(storageService)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultDatasafeServices.class)
    @Order(3)
    DefaultDatasafeServices multiDatasafeService(StorageService storageService, DFSConfig dfsConfig) {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerDefaultDatasafeServices
                .builder()
                .config(multiDfsConfig(datasafeProperties))
                .storage(multiStorageService(datasafeProperties))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(VersionedDatasafeServices.class)
    @Order(4)
    VersionedDatasafeServices versionedMultiDatasafeServices(StorageService storageService, DFSConfig dfsConfig) {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerVersionedDatasafeServices
                .builder()
                .config(multiDfsConfig(datasafeProperties))
                .storage(multiStorageService(datasafeProperties))
                .build();
    }

    //versioned multi dfs

    @Bean
    DFSConfig multiDfsConfig(DatasafeProperties properties) {
        return new MultiDFSConfig(URI.create(properties.getS3Path()), URI.create(properties.getDbProfilePath()));
    }

    /**
     * @return S3 based storage service
     */
    @Bean
    StorageService multiStorageService(DatasafeProperties properties) {
        StorageService db = new DatabaseStorageService(ALLOWED_TABLES, new DatabaseConnectionRegistry(
                ImmutableMap.of(properties.getDbUrl(),
                        new DatabaseCredentials(properties.getDbUsername(), properties.getDbPassword()))
        )
        );

        S3StorageService s3StorageService = new S3StorageService(s3(properties), properties.getBucketName(), Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors())
        );

        StorageService filesystem = new FileSystemStorageService(Paths.get(properties.getFsDevPath()));

        StorageService multiDfs = new SchemeDelegatingStorage(
                ImmutableMap.of(
                        //"file", filesystem,
                        "s3", s3StorageService,
                        "jdbc", db
                )
        );

        return multiDfs;
    }

    @Bean
    AmazonS3 s3(DatasafeProperties properties) {
        AmazonS3 amazonS3;

        boolean useEndpoint = properties.getAmazonUrl() != null;

        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(properties.getAmazonAccessKeyID(), properties.getAmazonSecretAccessKey())
        );

        if(useEndpoint) {
            amazonS3 = AmazonS3ClientBuilder
                    .standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(properties.getAmazonUrl(), properties.getAmazonRegion())
                    )
                    .withCredentials(credentialsProvider)
                    .enablePathStyleAccess()
                    .build();
        } else {
            amazonS3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(properties.getAmazonRegion())
                    .build();
        }

        if (!amazonS3.doesBucketExistV2(properties.getBucketName())) {
            amazonS3.createBucket(properties.getBucketName());
        }

        return amazonS3;
    }


}
