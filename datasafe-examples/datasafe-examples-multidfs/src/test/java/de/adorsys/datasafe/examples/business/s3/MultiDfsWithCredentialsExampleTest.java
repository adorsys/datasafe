package de.adorsys.datasafe.examples.business.s3;

import com.amazonaws.services.s3.AmazonS3;
import dagger.Lazy;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.impl.profile.config.DFSConfigWithStorageCreds;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.dfs.RegexAccessServiceWithStorageCredentialsImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.RegexDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3ClientFactory;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.shared.AwsClientRetry;
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import static de.adorsys.datasafe.examples.business.s3.MinioContainerId.DIRECTORY_BUCKET;
import static de.adorsys.datasafe.examples.business.s3.MinioContainerId.FILES_BUCKET_ONE;
import static de.adorsys.datasafe.examples.business.s3.MinioContainerId.FILES_BUCKET_TWO;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This example shows how client can register storage system and securely store its access details.
 * Here, we will use 2 Datasafe class instances - one for securely storing user access credentials
 * - configBucket and another is for accessing users' private files stored in
 * filesBucketOne, filesBucketTwo.
 */
@Slf4j
class MultiDfsWithCredentialsExampleTest {

    private static final String REGION = "eu-central-1";
    private static final ExecutorService EXECUTOR = ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService(4, 4);

    private static Map<MinioContainerId, GenericContainer> minios = new EnumMap<>(MinioContainerId.class);
    private static AmazonS3 directoryClient = null;
    private static Map<MinioContainerId, String> endpointsByHost = new HashMap<>();

    @BeforeAll
    static void startup() {
        // Create all required minio-backed S3 buckets:
        Arrays.stream(MinioContainerId.values()).forEach(it -> {
            GenericContainer minio = createAndStartMinio(it.getAccessKey(), it.getSecretKey());
            minios.put(it, minio);

            String endpoint = getDockerUri("http://127.0.0.1") + ":" + minio.getFirstMappedPort() + "/";
            endpointsByHost.put(it, endpoint + REGION + "/" + it.getBucketName() + "/");
            log.info("MINIO for {} is available at: {} with access: '{}'/'{}'", it, endpoint, it.getAccessKey(),
                    it.getSecretKey());

            AmazonS3 client = S3ClientFactory.getClient(
                    endpoint,
                    REGION,
                    it.getAccessKey(),
                    it.getSecretKey()
            );

            AwsClientRetry.createBucketWithRetry(client, it.getBucketName());

            if (it.equals(DIRECTORY_BUCKET)) {
                directoryClient = client;
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
        // BEGIN_SNIPPET:Datasafe with multi-dfs setup
        String directoryBucketS3Uri = "s3://" + DIRECTORY_BUCKET.getBucketName() + "/";
        // static client that will be used to access `directory` bucket:
        StorageService directoryStorage = new S3StorageService(
                directoryClient,
                DIRECTORY_BUCKET.getBucketName(),
                EXECUTOR
        );

        OverridesRegistry registry = new BaseOverridesRegistry();
        DefaultDatasafeServices multiDfsDatasafe = DaggerDefaultDatasafeServices
                .builder()
                .config(new DFSConfigWithStorageCreds(directoryBucketS3Uri, "PAZZWORT"::toCharArray))
                // This storage service will route requests to proper bucket based on URI content:
                // URI with directoryBucket to `directoryStorage`
                // URI with filesBucketOne will get dynamically generated S3Storage
                // URI with filesBucketTwo will get dynamically generated S3Storage
                .storage(
                        new RegexDelegatingStorage(
                                ImmutableMap.<Pattern, StorageService>builder()
                                    // bind URI that contains `directoryBucket` to directoryStorage
                                    .put(Pattern.compile(directoryBucketS3Uri + ".+"), directoryStorage)
                                    .put(
                                        Pattern.compile(getDockerUri("http://127.0.0.1") + ".+"),
                                        // Dynamically creates S3 client with bucket name equal to host value
                                        new UriBasedAuthStorageService(
                                            acc -> new S3StorageService(
                                                S3ClientFactory.getClient(
                                                    acc.getEndpoint(),
                                                    acc.getRegion(),
                                                    acc.getAccessKey(),
                                                    acc.getSecretKey()
                                                ),
                                                // Bucket name is encoded in first path segment
                                                acc.getBucketName(),
                                                EXECUTOR
                                            )
                                    )
                                ).build()
                        )
                )
                .overridesRegistry(registry)
                .build();
        // Instead of default BucketAccessService we will use service that reads storage access credentials from
        // keystore
        BucketAccessServiceImplRuntimeDelegatable.overrideWith(
            registry, args -> new WithCredentialProvider(args.getStorageKeyStoreOperations())
        );

        // John will have all his private files stored on `filesBucketOne` and `filesBucketOne`.
        // Depending on path of file - filesBucketOne or filesBucketTwo - requests will be routed to proper bucket.
        // I.e. path filesBucketOne/path/to/file will end up in `filesBucketOne` with key path/to/file
        // his profile and access credentials for `filesBucketOne`  will be in `configBucket`
        UserIDAuth john = new UserIDAuth("john", "secret"::toCharArray);
        // Here, nothing expects John has own storage credentials:
        multiDfsDatasafe.userProfile().registerUsingDefaults(john);

        // Tell system that John will use his own storage credentials - regex match:
        StorageIdentifier bucketOne = new StorageIdentifier(endpointsByHost.get(FILES_BUCKET_ONE) + ".+");
        StorageIdentifier bucketTwo = new StorageIdentifier(endpointsByHost.get(FILES_BUCKET_TWO) + ".+");
        // Set location for John's credentials keystore and put storage credentials into it:
        UserPrivateProfile profile = multiDfsDatasafe.userProfile().privateProfile(john);
        profile.getPrivateStorage().put(
            bucketOne,
            new AbsoluteLocation<>(BasePrivateResource.forPrivate(endpointsByHost.get(FILES_BUCKET_ONE) + "/"))
        );
        profile.getPrivateStorage().put(
            bucketTwo,
            new AbsoluteLocation<>(BasePrivateResource.forPrivate(endpointsByHost.get(FILES_BUCKET_TWO) + "/"))
        );
        multiDfsDatasafe.userProfile().updatePrivateProfile(john, profile);

        // register John's DFS access for `filesBucketOne` minio bucket
        multiDfsDatasafe.userProfile().registerStorageCredentials(
                john,
                bucketOne,
                new StorageCredentials(
                        FILES_BUCKET_ONE.getAccessKey(),
                        FILES_BUCKET_ONE.getSecretKey()
                )
        );
        // register John's DFS access for `filesBucketTwo` minio bucket
        multiDfsDatasafe.userProfile().registerStorageCredentials(
                john,
                bucketTwo,
                new StorageCredentials(
                        FILES_BUCKET_TWO.getAccessKey(),
                        FILES_BUCKET_TWO.getSecretKey()
                )
        );

        // Configuring multi-storage is done, user can use his multi-storage:

        // store this file on `filesBucketOne`
        try (OutputStream os = multiDfsDatasafe.privateService()
                .write(WriteRequest.forPrivate(john, bucketOne, "my/file.txt"))) {
            os.write("Content on bucket number ONE".getBytes(StandardCharsets.UTF_8));
        }

        // store this file on `filesBucketTwo`
        try (OutputStream os = multiDfsDatasafe.privateService()
                .write(WriteRequest.forPrivate(john, bucketTwo, "my/file.txt"))) {
            os.write("Content on bucket number TWO".getBytes(StandardCharsets.UTF_8));
        }

        // read file from `filesBucketOne`
        assertThat(multiDfsDatasafe.privateService()
                .read(ReadRequest.forPrivate(john, bucketOne, "my/file.txt"))
        ).hasContent("Content on bucket number ONE");

        // read file from `filesBucketTwo`
        assertThat(multiDfsDatasafe.privateService()
                .read(ReadRequest.forPrivate(john, bucketTwo, "my/file.txt"))
        ).hasContent("Content on bucket number TWO");
        // END_SNIPPET
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

    private static class WithCredentialProvider extends BucketAccessServiceImpl {

        @Delegate
        private final RegexAccessServiceWithStorageCredentialsImpl delegate;

        private WithCredentialProvider(Lazy<StorageKeyStoreOperations> storageKeyStoreOperations) {
            super(null);
            this.delegate = new RegexAccessServiceWithStorageCredentialsImpl(storageKeyStoreOperations);
        }
    }

    @SneakyThrows
    private static String getDockerUri(String defaultUri) {
        String dockerHost = System.getenv("DOCKER_HOST");
        if (dockerHost == null) {
            return defaultUri;
        }

        URI dockerUri = new URI(dockerHost);
        return "http://" + dockerUri.getHost();
    }
}
