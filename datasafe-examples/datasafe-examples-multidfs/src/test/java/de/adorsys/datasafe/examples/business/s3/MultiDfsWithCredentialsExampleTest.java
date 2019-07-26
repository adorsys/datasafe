package de.adorsys.datasafe.examples.business.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.storage.impl.s3.HostBasedBucketRouter;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This test shows how client can register storage system and securely store its access details.
 * Here, we will use 2 Datasafe class instances - one for securely storing user access credentials
 * - credentialsBucket and another is for accessing users' private files stored in
 * filesBucketOne, filesBucketTwo.
 */
class MultiDfsWithCredentialsExampleTest {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private DefaultDatasafeServices fileStorageDatasafe;
    private DefaultDatasafeServices credentialStorageDatasafe;

    private static Map<CONTAINER_ID, GenericContainer> minios = new EnumMap<>(CONTAINER_ID.class);
    private static AmazonS3 credentialClient;

    @BeforeAll
    static void startup() {
        Arrays.stream(CONTAINER_ID.values()).forEach(it -> {
            GenericContainer minio = createAndStartMinio(it.toString(), it.toString());
            minios.put(it, minio);

            AmazonS3 client = buildS3ForLocalMinio(minio.getFirstMappedPort(), it.toString(), it.toString());
            client.createBucket(it.toString());

            if (it.equals(CONTAINER_ID.CREDENTIALS_BUCKET)) {
                credentialClient = client;
            }
        });
    }

    @AfterAll
    static void shutdown() {
        minios.values().forEach(GenericContainer::stop);
    }

    @Test
    void testMultiUserStorageUserSetup() {
        credentialStorageDatasafe = DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig("s3://bucket/", "PAZZWORT"))
                .storage(new S3StorageService(
                        credentialClient,
                        CONTAINER_ID.CREDENTIALS_BUCKET.toString(),
                        executor)
                ).build();

        fileStorageDatasafe = DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig("s3://bucket/", "PAZZWORT"))
                .storage(new UriBasedAuthStorageService(
                        id -> {
                            String bucketName = id.getWithoutCreds().getHost();
                            return new S3StorageService(
                                    buildS3ForLocalMinio(
                                            minios.get(CONTAINER_ID.fromString(bucketName)).getFirstMappedPort(),
                                            id.getAccessKey(),
                                            id.getSecretKey()
                                    ),
                                    new HostBasedBucketRouter(),
                                    executor
                            );
                        }
                )).build();
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

    private static AmazonS3 buildS3ForLocalMinio(int port, String accessKey, String secretKey) {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://127.0.0.1:" + port, "eu-central-1")
                )
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(accessKey, secretKey)
                        )
                )
                .enablePathStyleAccess()
                .build();
    }

    private enum CONTAINER_ID {
        FILES_BUCKET_ONE,
        FILES_BUCKET_TWO,
        CREDENTIALS_BUCKET;


        @Override
        public String toString() {
            return super.toString().toLowerCase().replaceAll("_", "");
        }

        static CONTAINER_ID fromString(String value) {
            return Arrays.stream(CONTAINER_ID.values())
                    .filter(it -> it.toString().equals(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No mapping: " + value));
        }
    }
}
