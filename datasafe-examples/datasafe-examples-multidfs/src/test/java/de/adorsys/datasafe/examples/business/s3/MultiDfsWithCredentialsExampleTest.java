package de.adorsys.datasafe.examples.business.s3;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.StorageIdentifier;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.RegexDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static de.adorsys.datasafe.examples.business.s3.MinioContainerId.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This example shows how client can register storage system and securely store its access details.
 * Here, we will use 2 Datasafe class instances - one for securely storing user access credentials
 * - configBucket and another is for accessing users' private files stored in
 * filesBucketOne, filesBucketTwo.
 */
@Slf4j
class MultiDfsWithCredentialsExampleTest {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    private static Map<MinioContainerId, GenericContainer> minios = new EnumMap<>(MinioContainerId.class);
    private static AmazonS3 directoryClient = null;
    private static String s3DirectoryEndpoint = null;

    @BeforeAll
    static void startup() {
        System.setProperty("SECURE_LOGS", "off");
        System.setProperty("SECURE_SENSITIVE", "off");

        // Create all required minio-backed S3 buckets:
        Arrays.stream(MinioContainerId.values()).forEach(it -> {
            GenericContainer minio = createAndStartMinio(it.getAccessKey(), it.getSecretKey());
            minios.put(it, minio);

            String endpoint = "http://127.0.0.1:" + minio.getFirstMappedPort();
            log.info("MINIO for {} is available at: {} with access: '{}'/'{}'", it, endpoint, it.getAccessKey(),
                    it.getSecretKey());

            AmazonS3 client = S3ClientFactory.getClient(
                    endpoint,
                    it.getAccessKey(),
                    it.getSecretKey()
            );

            client.createBucket(it.getBucketName());

            if (it.equals(DIRECTORY_BUCKET)) {
                directoryClient = client;
                s3DirectoryEndpoint = endpoint;
            }
        });
    }

    @AfterAll
    static void shutdown() {
        minios.values().forEach(GenericContainer::stop);
    }

    @Test
    @SneakyThrows
    void testMultiUserStorageUserSetup() {
        String directoryBucketS3Uri = "s3://" + DIRECTORY_BUCKET.getBucketName() + "/";
        // static client that will be used to access `directory` bucket:
        StorageService directoryStorage = new S3StorageService(
                directoryClient,
                DIRECTORY_BUCKET.getBucketName(),
                EXECUTOR
        );

        DefaultDatasafeServices multiDfsDatasafe = DaggerDefaultDatasafeServices
                .builder()
                .config(new MultiDfsUser(directoryBucketS3Uri, "PAZZWORT", "s3://data/"))
                // This storage service will route requests to proper bucket based on URI content:
                .storage(
                        new RegexDelegatingStorage(
                                ImmutableMap.of(
                                        Pattern.compile(directoryBucketS3Uri + ".+"), directoryStorage,
                                        Pattern.compile("s3://data/.+"), directoryStorage
                                )
                        )
                ).build();

        // John will have all his private files stored on `filesBucketOne` and `filesBucketOne`.
        // Depending on path of file - filesBucketOne or filesBucketTwo - requests will be routed to proper bucket.
        // I.e. path filesBucketOne/path/to/file will end up in `filesBucketOne` with key path/to/file
        // his profile and access credentials for `filesBucketOne`  will be in `configBucket`
        UserIDAuth john = new UserIDAuth("john", "secret");
        multiDfsDatasafe.userProfile().registerUsingDefaults(john);
        // register John's DFS access for `filesBucketOne` minio bucket
        multiDfsDatasafe.userProfile().registerStorageCredentials(
                john,
                new StorageIdentifier(FILES_BUCKET_ONE.toString()),
                new StorageCredentials(
                        FILES_BUCKET_ONE.getAccessKey(),
                        FILES_BUCKET_ONE.getSecretKey()
                )
        );
        // register John's DFS access for `filesBucketTwo` minio bucket
        multiDfsDatasafe.userProfile().registerStorageCredentials(
                john,
                new StorageIdentifier(FILES_BUCKET_TWO.toString()),
                new StorageCredentials(
                        FILES_BUCKET_TWO.getAccessKey(),
                        FILES_BUCKET_TWO.getSecretKey()
                )
        );

        // store this file on `filesBucketOne`
        try (OutputStream os = multiDfsDatasafe.privateService()
                .write(WriteRequest.forDefaultPrivate(john, FILES_BUCKET_ONE.toString() + "/my/file.txt"))) {
            os.write("Content on bucket number ONE".getBytes(StandardCharsets.UTF_8));
        }

        // store this file on `filesBucketTwo`
        try (OutputStream os = multiDfsDatasafe.privateService()
                .write(WriteRequest.forDefaultPrivate(john, FILES_BUCKET_TWO.toString() + "/my/file.txt"))) {
            os.write("Content on bucket number TWO".getBytes(StandardCharsets.UTF_8));
        }

        // read file from `filesBucketOne`
        assertThat(multiDfsDatasafe.privateService()
                .read(ReadRequest.forDefaultPrivate(john, FILES_BUCKET_ONE.toString() + "/my/file.txt"))
        ).hasContent("Content on bucket number ONE");

        // read file from `filesBucketTwo`
        assertThat(multiDfsDatasafe.privateService()
                .read(ReadRequest.forDefaultPrivate(john, FILES_BUCKET_TWO.toString() + "/my/file.txt"))
        ).hasContent("Content on bucket number TWO");
    }

    private static GenericContainer createAndStartMinio(String accessKey, String secretKey) {
        GenericContainer minioContainer = new GenericContainer("minio/minio")
                .withExposedPorts(9000)
                .withEnv("MINIO_ACCESS_KEY", accessKey)
                .withEnv("MINIO_SECRET_KEY", secretKey)
                .withCommand("server /data")
                .waitingFor(Wait.defaultWaitStrategy());

        minioContainer.start();
        return minioContainer;
    }

    // user profile is going to be located under systemRoot but user files will be in other place
    private static class MultiDfsUser extends DefaultDFSConfig {

        private final String dataRoot;

        MultiDfsUser(String systemRoot, String systemPassword, String dataRoot) {
            super(systemRoot, systemPassword);
            this.dataRoot = dataRoot;
        }

        @Override
        protected Uri userRoot(UserID userID) {
            return new Uri(dataRoot).resolve(USERS_ROOT).resolve(userID.getValue() + "/");
        }
    }

}
