package de.adorsys.datasafe.business.impl.e2e;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.adorsys.datasafe.business.impl.storage.FileSystemStorageService;
import de.adorsys.datasafe.business.impl.storage.S3StorageService;
import de.adorsys.datasafe.business.impl.testcontainers.DaggerTestDocusafeServices;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@Slf4j
public class MinioTest extends StorageTest {
    private static String accessKeyID = "admin";
    private static String secretAccessKey = "password";
    private static String region = "eu-central-1";
    private static String bucketName = "home";
    private static String url = "http://localhost";
    private static BasicAWSCredentials creds = new BasicAWSCredentials(accessKeyID, secretAccessKey);
    private static AmazonS3 s3;

    private static GenericContainer minio = new GenericContainer("minio/minio")
            .withExposedPorts(9000)
            .withEnv("MINIO_ACCESS_KEY", "admin")
            .withEnv("MINIO_SECRET_KEY", "password")
            .withCommand("server /data")
            .waitingFor(Wait.defaultWaitStrategy());

    @BeforeAll
    static void beforeAll() {
        minio.start();
        Integer mappedPort = minio.getMappedPort(9000);
        log.info("Mapped port: " + mappedPort);
        s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url+":"+mappedPort, region))
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .enablePathStyleAccess()
                .build();

        s3.createBucket(bucketName);

    }
    @BeforeEach
    void init() {
        this.location = location;
        this.storage = new FileSystemStorageService(location);

        this.services = DaggerTestDocusafeServices.builder()
                .storageList(new S3StorageService(s3, bucketName))
                .storageRead(new S3StorageService(s3, bucketName))
                .storageWrite(new S3StorageService(s3, bucketName))
                .build();
    }
}
