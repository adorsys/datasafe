package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dagger.Lazy;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.impl.profile.config.DFSConfigWithStorageCreds;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.config.MultiDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.dfs.RegexAccessServiceWithStorageCredentialsImpl;
import de.adorsys.datasafe.storage.api.RegexDelegatingStorage;
import de.adorsys.datasafe.storage.api.SchemeDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import de.adorsys.datasafe.storage.impl.db.DatabaseConnectionRegistry;
import de.adorsys.datasafe.storage.impl.db.DatabaseCredentials;
import de.adorsys.datasafe.storage.impl.db.DatabaseStorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.storage.impl.s3.BucketNameRemovingRouter;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

/**
 * Configures default (non-versioned) Datasafe service that uses S3 client as storage provider.
 * Encryption provider: BouncyCastle.
 */
@Slf4j
@Configuration
public class DatasafeConfig {
    public static final String FILESYSTEM_ENV = "USE_FILESYSTEM";
    public static final String CLIENT_CREDENTIALS = "ALLOW_CLIENT_S3_CREDENTIALS";
    public static final String DATASAFE_S3_STORAGE = "DATASAFE_S3_STORAGE";

    private static final Set<String> ALLOWED_TABLES = ImmutableSet.of("private_profiles", "public_profiles");

    @Bean
    @ConditionalOnProperty(name = DATASAFE_S3_STORAGE, havingValue = "true")
    DFSConfig singleDfsConfigS3(DatasafeProperties properties) {
        return new DefaultDFSConfig(properties.getSystemRoot(), new ReadStorePassword(properties.getKeystorePassword()));
    }

    @Bean
    @ConditionalOnProperty(FILESYSTEM_ENV)
    DFSConfig singleDfsConfigFilesystem(DatasafeProperties properties) {
        return new DefaultDFSConfig(properties.getSystemRoot(), new ReadStorePassword(properties.getKeystorePassword()));
    }

    @Bean
    @ConditionalOnProperty(name = CLIENT_CREDENTIALS, havingValue = "true")
    DFSConfig withClientCredentials(DatasafeProperties properties) {
        return new DFSConfigWithStorageCreds(properties.getSystemRoot(), new ReadStorePassword(properties.getKeystorePassword()));
    }

    @Bean
    @ConditionalOnProperty(name = CLIENT_CREDENTIALS, havingValue = "true")
    OverridesRegistry withClientCredentialsOverrides() {
        OverridesRegistry registry = new BaseOverridesRegistry();
        BucketAccessServiceImplRuntimeDelegatable.overrideWith(registry, args ->
            new WithAccessCredentials(args.getStorageKeyStoreOperations()));
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean(DFSConfig.class)
    DFSConfig multiDfsConfig(DatasafeProperties properties) {
        return new MultiDFSConfig(URI.create(properties.getSystemRoot()), URI.create(properties.getDbUrl()),
                new ReadStorePassword(properties.getKeystorePassword()));
    }

    /**
     * @return Default implementation of Datasafe services.
     */
    @Bean
    DefaultDatasafeServices datasafeService(StorageService storageService, DFSConfig dfsConfig,
                                            Optional<OverridesRegistry> registry,
                                            DatasafeProperties properties) {

        return DaggerDefaultDatasafeServices
                .builder()
                .config(dfsConfig)
                .storage(storageService)
                .overridesRegistry(registry.orElse(null))
                .encryption(properties.getEncryption().toEncryptionConfig())
                .build();
    }

    @Bean
    VersionedDatasafeServices versionedDatasafeServices(StorageService storageService, DFSConfig dfsConfig,
                                                        Optional<OverridesRegistry> registry,
                                                        DatasafeProperties properties) {

        return DaggerVersionedDatasafeServices
                .builder()
                .config(dfsConfig)
                .storage(storageService)
                .overridesRegistry(registry.orElse(null))
                .encryption(properties.getEncryption().toEncryptionConfig())
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = CLIENT_CREDENTIALS, havingValue = "true")
    StorageService clientCredentials(AmazonS3 s3, S3Factory factory, DatasafeProperties properties) {
        ExecutorService executorService = ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService();
        S3StorageService basicStorage = new S3StorageService(
            s3,
            properties.getBucketName(),
            executorService
        );

        return new RegexDelegatingStorage(
            ImmutableMap.<Pattern, StorageService>builder()
                .put(Pattern.compile(properties.getSystemRoot() + ".+"), basicStorage)
                // here order is important, immutable map preserves key order, so properties.getAmazonUrl()
                // will be tried first
                .put(
                    Pattern.compile(".+"),
                    new UriBasedAuthStorageService(
                        acc -> new S3StorageService(
                                factory.getClient(
                                acc.getEndpoint(),
                                acc.getRegion(),
                                acc.getAccessKey(),
                                acc.getSecretKey()
                            ),
                            new BucketNameRemovingRouter(acc.getBucketName()),
                            executorService
                        )
                    )
                ).build()
        );
    }

    @Bean
    S3Factory factory() {
        return new BasicS3Factory();
    }

    /**
     * @return Filesystem based storage service
     */
    @Bean
    @ConditionalOnProperty(FILESYSTEM_ENV)
    StorageService singleStorageServiceFilesystem(DatasafeProperties properties) {
        String root = System.getenv(FILESYSTEM_ENV);
        log.info("==================== FILESYSTEM");
        log.info("build DFS to FILESYSTEM with root " + root);
        properties.setSystemRoot(root);
        return new FileSystemStorageService(Paths.get(root));
    }

    /**
     * @return S3 based storage service
     */
    @Bean
    @ConditionalOnProperty(name = DATASAFE_S3_STORAGE, havingValue = "true")
    StorageService singleStorageServiceS3(AmazonS3 s3, DatasafeProperties properties) {
        return new S3StorageService(
            s3,
            properties.getBucketName(),
            ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService()
        );
    }

    /**
     * @return storage service based on the two data storage. Profiles saving to the relation DB(like MySQL) and
     * data to the storage like Amazon S3, MinIO, CEPH
     */
    @Bean
    @ConditionalOnMissingBean(StorageService.class)
    StorageService multiStorageService(DatasafeProperties properties) {
        StorageService db = new DatabaseStorageService(ALLOWED_TABLES, new DatabaseConnectionRegistry(
                ImmutableMap.of(properties.getDbUrl(),
                        new DatabaseCredentials(properties.getDbUsername(), properties.getDbPassword()))
            )
        );

        S3StorageService s3StorageService = new S3StorageService(s3(properties), properties.getBucketName(),
                ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService()
        );

        return new SchemeDelegatingStorage(
                ImmutableMap.of(
                        "s3", s3StorageService,
                        "jdbc-mysql", db
                )
        );
    }

    @Bean
    @org.springframework.context.annotation.Lazy
    AmazonS3 s3(DatasafeProperties properties) {
        AmazonS3 amazonS3;

        boolean useEndpoint = properties.getAmazonUrl() != null;

        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(properties.getAmazonAccessKeyID(), properties.getAmazonSecretAccessKey())
        );

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider);

        if (useEndpoint) {
            builder = builder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                            properties.getAmazonUrl(),
                            properties.getAmazonRegion())
            ).enablePathStyleAccess();
        } else {
            builder.withRegion(properties.getAmazonRegion());
        }

        amazonS3 = builder.build();

        // used by local deployment in conjunction with minio
        if (useEndpoint && !amazonS3.doesBucketExistV2(properties.getBucketName())) {
            amazonS3.createBucket(properties.getBucketName());
        }

        return amazonS3;
    }

    private static class WithAccessCredentials extends BucketAccessServiceImpl {

        @Delegate
        private final RegexAccessServiceWithStorageCredentialsImpl access;

        private WithAccessCredentials(Lazy<StorageKeyStoreOperations> storageKeyStoreOperations) {
            super(null);
            this.access = new RegexAccessServiceWithStorageCredentialsImpl(storageKeyStoreOperations);
        }
    }
}
