package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.types.api.shared.DockerUtil.getDockerUri;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
//TODO: Extract stuff related container start/stop/clear to separate class. Used in datasafe-business and in datasafe-storage-impl-s3
class S3SystemStorageServiceIT extends BaseMockitoTest {

    private static final String FILE = "file";
    private static final String MESSAGE = "hello";

    private static String accessKeyID = "admin";
    private static String secretAccessKey = "password";
    private static String url = getDockerUri("http://localhost");
    private static AwsBasicCredentials creds = AwsBasicCredentials.create(accessKeyID, secretAccessKey);
    private static S3Client s3;
    private static AbsoluteLocation<PrivateResource> root;
    private static AbsoluteLocation<PrivateResource> fileWithMsg;

    private static GenericContainer minio;

    protected static String bucketName = "home";

    protected S3StorageService storageService;

    @BeforeAll
    static void beforeAll() {
        minio = new GenericContainer("minio/minio")
                .withExposedPorts(9000)
                .withEnv("MINIO_ACCESS_KEY", "admin")
                .withEnv("MINIO_SECRET_KEY", "password")
                .withCommand("server /data")
                .waitingFor(Wait.defaultWaitStrategy());

        minio.start();
        Integer mappedPort = minio.getMappedPort(9000);
        log.info("Mapped port: " + mappedPort);
        String region = "eu-central-1";
        s3 = S3Client.builder()
                .endpointOverride(URI.create(url + ":" + mappedPort))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .region(Region.of(region))
                .forcePathStyle(true)
                .build();

        s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        root = new AbsoluteLocation<>(BasePrivateResource.forPrivate(new Uri("s3://" + bucketName)));
        fileWithMsg = new AbsoluteLocation<>(BasePrivateResource.forPrivate(new Uri("./" + FILE))
                .resolveFrom(root));
    }

    @BeforeEach
    void init() {
        this.storageService = new S3StorageService(
                s3,
                bucketName,
                ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService()
        );
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
    void testListOutOfStandardListFilesLimit() {
        int numberOfFilesOverLimit = 1010;
        for (int i = 0; i < numberOfFilesOverLimit; i++) {
            s3.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key("over_limit/" + FILE + i)
                    .build(), RequestBody.fromString(MESSAGE));

            log.trace("Save #" + i + " file");
        }
        List<AbsoluteLocation<PrivateResource>> allFiles = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("over_limit/")
                    .maxKeys(1000);

            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }

            ListObjectsV2Response response = s3.listObjectsV2(requestBuilder.build());

            response.contents().forEach(s3Object -> {
                try {
                    allFiles.add(new AbsoluteLocation<>(
                            BasePrivateResource.forPrivate(new URI("s3://" + bucketName + "/" + s3Object.key()))));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });

            continuationToken = response.nextContinuationToken();

        } while (continuationToken != null);

        assertThat(allFiles).hasSize(numberOfFilesOverLimit);
    }
    @Test
    void listDeepLevel() {
        s3.putObject(PutObjectRequest.builder().bucket(bucketName).key("root.txt").build(),
                RequestBody.fromString("txt1"));
        s3.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key("deeper/level1.txt")
                .build(), RequestBody.fromString("txt2"));
        s3.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key("deeper/more/level2.txt")
                .build(), RequestBody.fromString("txt3"));


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
        try (OutputStream os = storageService.write(WithCallback.noCallback(fileWithMsg))) {
            os.write(MESSAGE.getBytes());
        }

        assertThat(storageService.read(fileWithMsg)).hasContent(MESSAGE);
    }

    @Test
    void remove() {
        createFileWithMessage();

        storageService.remove(fileWithMsg);

        assertThrows(NoSuchKeyException.class, () -> s3.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(FILE)
                .build()));
    }

    @Test
    void removeCascades() {
        createFileWithMessage("root/file1.txt");
        createFileWithMessage("root/file2.txt");

        AbsoluteLocation<PrivateResource> rootOfFiles = new AbsoluteLocation<>(BasePrivateResource.forPrivate(new Uri("s3://" + bucketName + "/root/")));

        storageService.remove(rootOfFiles);

        assertThrows(NoSuchKeyException.class, () -> s3.getObject(GetObjectRequest.builder().bucket(bucketName).key("root/file1.txt").build()));
        assertThrows(NoSuchKeyException.class, () -> s3.getObject(GetObjectRequest.builder().bucket(bucketName).key("root/file2.txt").build()));
    }

    @SneakyThrows
    private void createFileWithMessage(String path) {
        s3.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(path)
                .build(), RequestBody.fromString(MESSAGE));
    }

    @SneakyThrows
    private void createFileWithMessage() {
        createFileWithMessage(FILE);
    }

    @AfterEach
    @SneakyThrows
    void cleanup() {
        log.info("Executing cleanup");
        if (null != minio) {
            removeObjectFromS3(s3, bucketName, "");
        }
    }

    private void removeObjectFromS3(S3Client s3, String bucket, String prefix) {
        s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build())
                .contents()
                .forEach(it -> {
                    log.debug("Remove {}", it.key());
                    s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(it.key()).build());
                });
    }
    @AfterAll
    public static void afterAll() {
        log.info("Stopping containers");
        if (null != minio) {
            minio.stop();
            minio = null;
        }
    }
}
