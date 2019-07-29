package de.adorsys.datasafe.examples.business.s3;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static de.adorsys.datasafe.examples.business.s3.MinioContainerId.FILES_BUCKET_ONE;
import static de.adorsys.datasafe.examples.business.s3.MinioContainerId.FILES_BUCKET_TWO;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This example shows how client can register storage system and securely store its access details.
 * Here, we will use 2 Datasafe class instances - one for securely storing user access credentials
 * - credentialsBucket and another is for accessing users' private files stored in
 * filesBucketOne, filesBucketTwo.
 */
@Slf4j
class MultiDfsWithCredentialsExampleTest {

    private static Map<MinioContainerId, GenericContainer> minios = new EnumMap<>(MinioContainerId.class);
    private static AmazonS3 credentialClient;
    private static String s3CredentialsEndpoint;

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

            if (it.equals(MinioContainerId.CREDENTIALS_BUCKET)) {
                credentialClient = client;
                s3CredentialsEndpoint = endpoint;
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
        MultiDfsDatasafe multiDfsDatasafe =
                new MultiDfsDatasafe(credentialClient, MinioContainerId.CREDENTIALS_BUCKET.getBucketName());

        // John will have all his private files stored on `filesBucketOne` and `filesBucketOne`.
        // Depending on path of file - filesBucketOne or filesBucketTwo - requests will be routed to proper bucket.
        // I.e. path filesBucketOne/path/to/file will end up in `filesBucketOne` with key path/to/file
        // his profile and access credentials for `filesBucketOne`  will be in `credentialsBucket`
        UserIDAuth john = new UserIDAuth("john", "secret");
        multiDfsDatasafe.userProfile().registerUsingDefaults(john);
        // register John's DFS access for `filesBucketOne` minio bucket
        multiDfsDatasafe.registerDfs(
                john,
                FILES_BUCKET_ONE.toString(),
                new StorageCredentials(
                        s3CredentialsEndpoint,
                        FILES_BUCKET_ONE.getAccessKey(),
                        FILES_BUCKET_ONE.getSecretKey()
                )
        );
        // register John's DFS access for `filesBucketTwo` minio bucket
        multiDfsDatasafe.registerDfs(
                john,
                MinioContainerId.FILES_BUCKET_TWO.toString(),
                new StorageCredentials(
                        s3CredentialsEndpoint,
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

}
