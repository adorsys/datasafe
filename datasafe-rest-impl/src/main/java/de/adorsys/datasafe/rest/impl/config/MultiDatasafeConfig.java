package de.adorsys.datasafe.rest.impl.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DaggerVersionedDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.impl.profile.config.MultiDFSConfig;
import de.adorsys.datasafe.storage.api.SchemeDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.db.DatabaseConnectionRegistry;
import de.adorsys.datasafe.storage.impl.db.DatabaseCredentials;
import de.adorsys.datasafe.storage.impl.db.DatabaseStorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Set;
import java.util.concurrent.Executors;

@Configuration
@Conditional(MultiDfsCondition.class)
public class MultiDatasafeConfig {

    private static final Set<String> ALLOWED_TABLES = ImmutableSet.of("users", "private_profiles", "public_profiles");

    private Path fsPath;
    private StorageService filesystem;
    private StorageService db;
    private DefaultDatasafeServices datasafeServices;
    private DatasafeProperties datasafeProperties;

    @Inject
    MultiDatasafeConfig(DatasafeProperties datasafeProperties) {
        this.datasafeProperties = datasafeProperties;
    }

    @Bean
    DFSConfig multiDfsConfig(DatasafeProperties properties) {
        return new MultiDFSConfig(URI.create(properties.getS3Path()t ), URI.create(properties.getDbProfilePath()));
    }

    /**
     * @return S3 based storage service
     */
    @Bean
    StorageService multiStorageService(DatasafeProperties properties) {
        StorageService db = new DatabaseStorageService(ALLOWED_TABLES, new DatabaseConnectionRegistry(
                uri -> uri.location().getWrapped().getScheme() + ":" + uri.location().getPath().split("/")[1],
                ImmutableMap.of("jdbc://localhost:9999", new DatabaseCredentials("sa", "sa")))
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
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(properties.getAmazonAccessKeyID(), properties.getAmazonSecretAccessKey()))
                )
                .withRegion(properties.getAmazonRegion())
                .build();
    }

    /**
     * @return Default implementation of Datasafe services.
     */
    @Bean
    DefaultDatasafeServices multiDatasafeService() {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerDefaultDatasafeServices
                .builder()
                .config(multiDfsConfig(datasafeProperties))
                .storage(multiStorageService(datasafeProperties))
                .build();
    }

    @Bean
    VersionedDatasafeServices versionedDatasafeServices() {

        Security.addProvider(new BouncyCastleProvider());

        return DaggerVersionedDatasafeServices
                .builder()
                .config(multiDfsConfig(datasafeProperties))
                .storage(multiStorageService(datasafeProperties))
                .build();
    }
}
