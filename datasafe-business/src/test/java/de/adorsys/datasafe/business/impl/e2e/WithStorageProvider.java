package de.adorsys.datasafe.business.impl.e2e;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.base.Suppliers;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.impl.storage.FileSystemStorageService;
import de.adorsys.datasafe.business.impl.storage.S3StorageService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public abstract class WithStorageProvider extends BaseE2ETest {

    private static String minioAccessKeyID = "admin";
    private static String minioSecretAccessKey = "password";
    private static String minioRegion = "eu-central-1";
    private static String minioBucketName = "home";
    private static String minioUrl = "http://localhost";
    private static String prefix = UUID.randomUUID().toString();

    private static String amazonAccessKeyID = System.getProperty("AWS_ACCESS_KEY");
    private static String amazonSecretAccessKey = System.getProperty("AWS_SECRET_KEY");
    private static String amazonRegion = System.getProperty("AWS_REGION", "eu-central-1");
    private static String amazonBucket = System.getProperty("AWS_BUCKET", "adorsys-docusafe");

    private static GenericContainer minioContainer;

    private static Path tempDir;
    private static AmazonS3 minio;
    private static AmazonS3 amazonS3;

    private static Supplier<Void> MINIO = Suppliers.memoize(() -> {
        startMinio();
        return null;
    });

    private static Supplier<Void> AMAZON_S3 = Suppliers.memoize(() -> {
        initS3();
        return null;
    });

    @BeforeAll
    static void init(@TempDir Path tempDir) {
        WithStorageProvider.tempDir = tempDir;
    }

    @AfterEach
    @SneakyThrows
    void cleanup() {
        if (null != tempDir && tempDir.toFile().exists()) {
            FileUtils.cleanDirectory(tempDir.toFile());
        }

        if (null != minio) {
            removeObjectFromS3(minio, minioBucketName, prefix);
        }

        if (null != amazonS3) {
            removeObjectFromS3(amazonS3, amazonBucket, prefix);
        }
    }

    @AfterAll
    static void shutdown() {
        if (null != minioContainer) {
            minioContainer.stop();
        }
    }

    @ValueSource
    protected static Stream<WithStorageProvider.StorageDescriptor> allStorages() {
        return Stream.of(
                new StorageDescriptor(
                        "FILESYSTEM",
                        () -> new FileSystemStorageService(tempDir.toUri()),
                        tempDir.toUri()
                ),
                minio(),
                s3()
        ).filter(Objects::nonNull);
    }

    @ValueSource
    protected static StorageDescriptor minio() {
        return new StorageDescriptor(
                "MINIO S3",
                () -> {
                    MINIO.get();
                    return new S3StorageService(minio, minioBucketName);
                },
                URI.create("s3://" + minioBucketName + "/" + prefix + "/")
        );
    }

    @ValueSource
    protected static StorageDescriptor s3() {
        if (null == amazonAccessKeyID) {
            return null;
        }

        return new StorageDescriptor(
                "AMAZON S3",
                () -> {
                    AMAZON_S3.get();
                    return new S3StorageService(amazonS3, amazonBucket);
                },
                URI.create("s3://" + amazonBucket + "/" + prefix + "/")
        );
    }

    private void removeObjectFromS3(AmazonS3 amazonS3, String bucket, String prefix) {
        amazonS3.listObjects(bucket, prefix)
                .getObjectSummaries()
                .forEach(it -> {
                    log.debug("Remove {}", it.getKey());
                    amazonS3.deleteObject(bucket, it.getKey());
                });
    }

    private static void initS3() {
        if (StringUtils.isBlank(amazonAccessKeyID)) {
            return;
        }

        amazonS3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(amazonAccessKeyID, amazonSecretAccessKey))
                )
                .withRegion(amazonRegion)
                .build();
    }

    private static void startMinio() {
        minioContainer = new GenericContainer("minio/minio")
                .withExposedPorts(9000)
                .withEnv("MINIO_ACCESS_KEY", "admin")
                .withEnv("MINIO_SECRET_KEY", "password")
                .withCommand("server /data")
                .waitingFor(Wait.defaultWaitStrategy());

        minioContainer.start();
        Integer mappedPort = minioContainer.getMappedPort(9000);
        log.info("Mapped port: " + mappedPort);
        minio = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(minioUrl + ":" + mappedPort, minioRegion)
                )
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(minioAccessKeyID, minioSecretAccessKey)
                        )
                )
                .enablePathStyleAccess()
                .build();


        minio.createBucket(minioBucketName);
    }

    @Getter
    @ToString(of = "name")
    static class StorageDescriptor {

        private final String name;
        private final Supplier<StorageService> storageService;
        private final URI location;

        StorageDescriptor(String name, Supplier<StorageService> storageService, URI location) {
            this.name = name;
            this.storageService = storageService;
            this.location = location;
        }
    }
}
