package de.adorsys.datasafe.storage.impl.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import de.adorsys.datasafe.types.api.resource.*;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class S3SystemStorageServiceTest extends BaseMockitoTest {

    private static final String FILE = "file";
    private static final String MESSAGE = "hello";

    private static String accessKeyID = "admin";
    private static String secretAccessKey = "password";
    private static String region = "eu-central-1";
    private static String bucketName = "home";
    private static String url = "http://localhost";
    private static BasicAWSCredentials creds = new BasicAWSCredentials(accessKeyID, secretAccessKey);
    private static AmazonS3 s3;
    private static AbsoluteLocation<PrivateResource> root;
    private static AbsoluteLocation<PrivateResource> fileWithMsg;

    private static GenericContainer minio = new GenericContainer("minio/minio")
            .withExposedPorts(9000)
            .withEnv("MINIO_ACCESS_KEY", "admin")
            .withEnv("MINIO_SECRET_KEY", "password")
            .withCommand("server /data")
            .waitingFor(Wait.defaultWaitStrategy());

    private S3StorageService storageService;

    @BeforeAll
    static void beforeAll() {
        minio.start();
        Integer mappedPort = minio.getMappedPort(9000);
        log.info("Mapped port: " + mappedPort);
        s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url + ":" + mappedPort, region))
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .enablePathStyleAccess()
                .build();

        s3.createBucket(bucketName);
        root = new AbsoluteLocation<>(BasePrivateResource.forPrivate(new Uri("s3://" + bucketName)));
        fileWithMsg = new AbsoluteLocation<>(BasePrivateResource.forPrivate(new Uri("./" + FILE))
                .resolveFrom(root));
    }

    @BeforeEach
    void init() {
        this.storageService = new S3StorageService(s3, bucketName);
    }

    @Test
    void list() {
        createFileWithMessage();

        assertThat(storageService.list(root))
                .hasSize(1)
                .extracting(AbsoluteLocation::location)
                .asString().contains(FILE);
    }

    @Test
    void listDeepLevel() {
        s3.putObject(bucketName, "root.txt", "txt1");
        s3.putObject(bucketName, "deeper/level1.txt", "txt2");
        s3.putObject(bucketName, "deeper/more/level2.txt", "txt3");

        List<AbsoluteLocation<ResolvedResource>> resources = storageService.list(
                new AbsoluteLocation<>(BasePrivateResource.forPrivate(new Uri("s3://" + bucketName + "/deeper")))
        ).collect(Collectors.toList());

        assertThat(resources)
                .extracting(AbsoluteLocation::location)
                .extracting(Uri::toASCIIString)
                .containsExactlyInAnyOrder(
                        "s3://" + bucketName + "/deeper/level1.txt",
                        "s3://" + bucketName + "/deeper/more/level2.txt"
                );
    }

    @Test
    void listOnNonExisting() {
        assertThat(storageService.list(root)).isEmpty();
    }

    @Test
    void read() {
        createFileWithMessage();

        assertThat(storageService.read(fileWithMsg)).hasContent(MESSAGE);
    }

    @Test
    @SneakyThrows
    void write() {
        try (OutputStream os = storageService.write(fileWithMsg)) {
            os.write(MESSAGE.getBytes());
        }

        assertThat(storageService.read(fileWithMsg)).hasContent(MESSAGE);
    }

    @Test
    void remove() {
        createFileWithMessage();

        storageService.remove(fileWithMsg);

        assertThrows(AmazonS3Exception.class, () -> s3.getObject(bucketName, FILE));
    }

    @SneakyThrows
    private void createFileWithMessage() {
        s3.putObject(bucketName, FILE, MESSAGE);
    }
}
