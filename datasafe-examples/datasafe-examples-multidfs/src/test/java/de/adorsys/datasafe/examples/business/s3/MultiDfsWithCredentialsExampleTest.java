package de.adorsys.datasafe.examples.business.s3;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import lombok.Getter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * This test shows how client can register storage system and securely store its access details.
 * Here, we will use 2 Datasafe class instances - one for securely storing user access credentials
 * - credentialsBucket and another is for accessing users' private files stored in
 * filesBucketOne, filesBucketTwo.
 */
class MultiDfsWithCredentialsExampleTest {

    private static Map<MinioContainer, GenericContainer> minios = new EnumMap<>(MinioContainer.class);
    private static AmazonS3 credentialClient;
    private static String s3CredentialsEndpoint;

    @BeforeAll
    static void startup() {
        Arrays.stream(MinioContainer.values()).forEach(it -> {
            GenericContainer minio = createAndStartMinio(it.getAccessKey(), it.getSecretKey());
            minios.put(it, minio);

            String endpoint = "http://127.0.0.1:" + minio.getFirstMappedPort();

            AmazonS3 client = S3ClientFactory.getClient(
                    endpoint,
                    it.getAccessKey(),
                    it.getSecretKey()
            );

            client.createBucket(it.getBucketName());

            if (it.equals(MinioContainer.CREDENTIALS_BUCKET)) {
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
    void testMultiUserStorageUserSetup() {
        MultiDfsDatasafe multiDfsDatasafe =
                new MultiDfsDatasafe(credentialClient, MinioContainer.CREDENTIALS_BUCKET.getBucketName());

        UserIDAuth john = new UserIDAuth("john", "secret");
        multiDfsDatasafe.userProfile().registerUsingDefaults(john);
        multiDfsDatasafe.registerDfs(
                john,
                MinioContainer.FILES_BUCKET_ONE.toString(),
                new MultiDfsDatasafe.StorageCredentials(
                        s3CredentialsEndpoint,
                        MinioContainer.CREDENTIALS_BUCKET.getAccessKey(),
                        MinioContainer.CREDENTIALS_BUCKET.getSecretKey()
                )
        );
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

    @Getter
    private enum MinioContainer {
        FILES_BUCKET_ONE,
        FILES_BUCKET_TWO,
        CREDENTIALS_BUCKET;

        private final String accessKey;
        private final String secretKey;
        private final String bucketName;

        MinioContainer() {
            this.accessKey = "access-" + toString();
            this.secretKey = "secret-" + toString();
            this.bucketName = toString();
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase().replaceAll("_", "");
        }
    }
}
