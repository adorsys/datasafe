package de.adorsys.datasafe.business.impl.e2e;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.storage.FileSystemStorageService;
import de.adorsys.datasafe.business.impl.storage.S3StorageService;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public abstract class WithStorageProvider extends BaseE2ETest {

    private static String minioAccessKeyID = "admin";
    private static String minioSecretAccessKey = "password";
    private static String minioRegion = "eu-central-1";
    private static String minioBucketName = "home";
    private static String minioUrl = "http://localhost";

    private static String amazonAccessKeyID = System.getProperty("AWS_ACCESS_KEY");
    private static String amazonSecretAccessKey = System.getProperty("AWS_SECRET_KEY");
    private static String amazonRegion = System.getProperty("AWS_REGION", "eu-central-1");
    private static String amazonBucket = System.getProperty("AWS_BUCKET", "adorsys-docusafe");

    private static GenericContainer minioContainer = new GenericContainer("minio/minio")
            .withExposedPorts(9000)
            .withEnv("MINIO_ACCESS_KEY", "admin")
            .withEnv("MINIO_SECRET_KEY", "password")
            .withCommand("server /data")
            .waitingFor(Wait.defaultWaitStrategy());

    private static Path tempDir;
    private static AmazonS3 minio;
    private static AmazonS3 amazonS3;

    @BeforeAll
    static void init(@TempDir Path tempDir) {
        minioContainer.start();
        Integer mappedPort = minioContainer.getMappedPort(9000);
        log.info("Mapped port: " + mappedPort);
        minio = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(minioUrl + ":" + mappedPort, minioRegion))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(minioAccessKeyID, minioSecretAccessKey)))
                .enablePathStyleAccess()
                .build();


        minio.createBucket(minioBucketName);
        WithStorageProvider.tempDir = tempDir;
        initS3();
    }

    @AfterAll
    static void detach() {
        minioContainer.stop();
    }

    @ValueSource
    protected static Stream<WithStorageProvider.StorageDescriptor> storages() {
        return Stream.of(
                new StorageDescriptor("FILESYSTEM", new FileSystemStorageService(tempDir.toUri()), tempDir.toUri()),
                minio(),
                s3()
        ).filter(Objects::nonNull);
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

    private static StorageDescriptor minio() {
        if (null == minio) {
            return null;
        }

        return new StorageDescriptor(
                "MINIO S3",
                new S3StorageService(minio, minioBucketName), URI.create("s3://" +  minioBucketName + "/")
        );
    }

    private static StorageDescriptor s3() {
        if (null == amazonS3) {
            return null;
        }

        return new StorageDescriptor(
                "AMAZON S3",
                new S3StorageService(amazonS3, amazonBucket), URI.create("s3://" +  amazonBucket + "/")
        );
    }

    @Getter
    @ToString(of = "name")
    static class StorageDescriptor {

        private final String name;
        private final StorageService storageService;
        private final URI location;
        private final DefaultDatasafeServices docusafeServices;

        StorageDescriptor(String name, StorageService storageService, URI location) {
            this.name = name;
            this.storageService = storageService;
            this.location = location;
            this.docusafeServices = DaggerDefaultDatasafeServices
                    .builder()
                    .config(new DFSConfig() {
                        @Override
                        public String keystorePassword() {
                            return "PAZZWORD";
                        }

                        @Override
                        public URI systemRoot() {
                            return location;
                        }
                    })
                    .storageList(storageService)
                    .storageRead(storageService)
                    .storageWrite(storageService)
                    .storageRemove(storageService)
                    .storageCheck(storageService)
                    .build();
        }
    }
}
