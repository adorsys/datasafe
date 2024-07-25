package de.adorsys.datasafe.examples.business.s3;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.callback.PhysicalVersionCallback;
import de.adorsys.datasafe.types.api.resource.StorageVersion;
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This test shows simplistic usage of Datasafe default services that reside on versioned storage system.
 */
@Slf4j
@DisabledIfSystemProperty(named = "SKIP_CEPH", matches = "true")
class BaseUserOperationsWithDefaultDatasafeOnVersionedStorageIT{

    private static final String MY_OWN_FILE_TXT = "my/own/file.txt";

    private static final String VERSIONED_BUCKET_NAME = "home";
    private static final String ACCESS_KEY = "access";
    private static final String SECRET_KEY = "secret";

    private static GenericContainer cephContainer;
    private static S3Client cephS3;
    private static String cephMappedUrl;

    private DefaultDatasafeServices defaultDatasafeServices;

    /**
     * This creates CEPH Rados gateway in docker container and creates S3 client for it.
     */
    @BeforeAll
    static void createServices() {
        log.info("Starting CEPH");
        // Create CEPH container:
        cephContainer = new GenericContainer("ceph/daemon")
                .withExposedPorts(8000, 5000)
                .withEnv("RGW_FRONTEND_PORT", "8000")
                .withEnv("SREE_PORT", "5000")
                .withEnv("DEBUG", "verbose")
                .withEnv("CEPH_DEMO_UID", "nano")
                .withEnv("MON_IP", "127.0.0.1")
                .withEnv("CEPH_PUBLIC_NETWORK", "0.0.0.0/0")
                .withEnv("CEPH_DAEMON", "demo")
                .withEnv("DEMO_DAEMONS", "mon,mgr,osd,rgw")
                .withEnv("CEPH_DEMO_ACCESS_KEY", ACCESS_KEY)
                .withEnv("CEPH_DEMO_SECRET_KEY", SECRET_KEY)
                .withCommand("mkdir -p /etc/ceph && mkdir -p /var/lib/ceph && /entrypoint.sh")
                .waitingFor(Wait.defaultWaitStrategy());

        cephContainer.start();
        Integer mappedPort = cephContainer.getMappedPort(8000);
        // URL for S3 API/bucket root:
        cephMappedUrl = getDockerUri("http://0.0.0.0") + ":" + mappedPort;
        log.info("Ceph mapped URL: {}", cephMappedUrl);
        cephS3 = S3Client.builder()
                .endpointOverride(URI.create(cephMappedUrl))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
                ))
                .build();

        // Create bucket in CEPH that will support versioning
        cephS3.createBucket(CreateBucketRequest.builder()
                .bucket(VERSIONED_BUCKET_NAME)
                .build());
        cephS3.setBucketVersioning(SetBucketVersioningConfigurationRequest.builder()
                .bucket(VERSIONED_BUCKET_NAME)
                .versioningConfiguration(BucketVersioningConfiguration.builder()
                        .status(BucketVersioningConfiguration.Status.ENABLED)
                        .build())
                .build());
    }

    @AfterAll
    static void stopCeph() {
        cephContainer.stop();
    }

    @BeforeEach
    void init() {
        // this will create all Datasafe files and user documents under S3 bucket root, we assume that
        // S3 versioned bucket was already created
        defaultDatasafeServices = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(cephMappedUrl, "secret"::toCharArray))
                .storage(new S3StorageService(
                        cephS3,
                        VERSIONED_BUCKET_NAME,
                        ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService()))
                .build();
    }

    /**
     * S3 storage adapter supports sending back file version (if S3 storage returns it) when storing object to
     * bucket and it allows reading object using its version too.
     */
    @Test
    @SneakyThrows
    void writeFileThenReadLatestAndReadByVersion() {
        // BEGIN_SNIPPET:Versioned storage support - writing file and reading back
        // creating new user
        UserIDAuth user = registerUser("john");

        // writing data to my/own/file.txt 3 times with different content:
        // 1st time, writing into my/own/file.txt:
        // Expanded snippet of how to capture file version when writing object:
        AtomicReference<String> version = new AtomicReference<>();
        try (OutputStream os = defaultDatasafeServices.privateService()
                .write(WriteRequest.forDefaultPrivate(user, MY_OWN_FILE_TXT)
                        .toBuilder()
                        .callback((PhysicalVersionCallback) version::set)
                        .build())
        ) {
            // Initial version will contain "Hello 1":
            os.write("Hello 1".getBytes(StandardCharsets.UTF_8));
        }
        // this variable has our initial file version:
        String version1 = version.get();

        // Write 2 more times different data to same file - my/own/file.txt:
        String version2 = writeToPrivate(user, MY_OWN_FILE_TXT, "Hello 2");
        // Last version will contain "Hello 3":
        String version3 = writeToPrivate(user, MY_OWN_FILE_TXT, "Hello 3");

        // now, when we read file without specifying version - we see latest file content:
        assertThat(defaultDatasafeServices.privateService().read(
                ReadRequest.forDefaultPrivate(user, MY_OWN_FILE_TXT))
        ).hasContent("Hello 3");

        // but if we specify file version - we get content for it:
        assertThat(defaultDatasafeServices.privateService().read(
                ReadRequest.forDefaultPrivateWithVersion(user, MY_OWN_FILE_TXT, new StorageVersion(version1)))
        ).hasContent("Hello 1");
        // END_SNIPPET

        log.debug("version 1 " + version1);
        log.debug("version 2 " + version2);
        log.debug("version 3 " + version3);
        assertThat(defaultDatasafeServices.privateService().list(ListRequest.forDefaultPrivate(user, ""))).hasSize(1);
        assertThat(version1.equals(version2)).isFalse();
        assertThat(version1.equals(version3)).isFalse();
    }

    /**
     * Example of how to remove specific version id
     */
    @Test
    @SneakyThrows
    void removeSpecificVersionId() {
        // BEGIN_SNIPPET:Versioned storage support - removing specific version
        // creating new user
        UserIDAuth user = registerUser("john");

        // writing data to my/own/file.txt 2 times with different content:
        String versionId = writeToPrivate(user, MY_OWN_FILE_TXT, "Hello 1");
        writeToPrivate(user, MY_OWN_FILE_TXT, "Hello 2");

        // now, we read old file version
        assertThat(new String(cephS3.getObject(GetObjectRequest.builder()
                        .bucket(VERSIONED_BUCKET_NAME)
                        .key(MY_OWN_FILE_TXT)
                        .versionId(versionId)
                        .build())
                .readAllBytes(), StandardCharsets.UTF_8))
                .isEqualTo("Hello 1");

        // now, we remove old file version
        cephS3.deleteObject(DeleteObjectRequest.builder()
                .bucket(VERSIONED_BUCKET_NAME)
                .key(MY_OWN_FILE_TXT)
                .versionId(versionId)
                .build());

        // it is removed from storage, so when we read it we get exception
        assertThatThrownBy(() -> cephS3.getObject(GetObjectRequest.builder()
                .bucket(VERSIONED_BUCKET_NAME)
                .key(MY_OWN_FILE_TXT)
                .versionId(versionId)
                .build()))
                .isInstanceOf(NoSuchKeyException.class);

        // but latest file version is still available
        assertThat(new String(cephS3.getObject(GetObjectRequest.builder()
                        .bucket(VERSIONED_BUCKET_NAME)
                        .key(MY_OWN_FILE_TXT)
                        .build())
                .readAllBytes(), StandardCharsets.UTF_8))
                .isEqualTo("Hello 2");
        // END_SNIPPET
    }

    @SneakyThrows
    private String writeToPrivate(UserIDAuth user, String path, String fileContent) {
        AtomicReference<String> version = new AtomicReference<>();
        try (OutputStream os = defaultDatasafeServices.privateService()
                .write(WriteRequest.forDefaultPrivate(user, path)
                        .toBuilder()
                        .callback((PhysicalVersionCallback) version::set)
                        .build())
        ) {
            os.write(fileContent.getBytes(StandardCharsets.UTF_8));
        }

        return version.get();
    }

    private UserIDAuth registerUser(String username) {
        UserIDAuth creds = new UserIDAuth(username, ("passwrd" + username)::toCharArray);
        defaultDatasafeServices.userProfile().registerUsingDefaults(creds);
        return creds;
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
