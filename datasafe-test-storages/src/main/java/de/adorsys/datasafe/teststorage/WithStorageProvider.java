package de.adorsys.datasafe.teststorage;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.util.StringUtils;
import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.teststorage.monitoring.ThreadPoolStatus;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * Provides different storage types - filesystem, minio, etc. to be used in tests.
 */
@Slf4j
@Getter
public abstract class WithStorageProvider extends BaseMockitoTest {
    public static final String SKIP_CEPH = "SKIP_CEPH";

    private static String bucketPath =  UUID.randomUUID().toString();

    private static final ExecutorService EXECUTOR_SERVICE =
            ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService(4, 4);

    private static String minioAccessKeyID = "admin";
    private static String minioSecretAccessKey = "password";
    private static String minioRegion = "eu-central-1";
    private static String minioBucketName = "home";
    private static String minioUrl = "http://localhost";
    private static String minioMappedUrl;

    // Note that CEPH is used to test bucket-level versioning, so you will get versioned bucket:
    private static String cephAccessKeyID = "admin";
    private static String cephSecretAccessKey = "password";
    private static String cephRegion = "eu-central-1";
    private static String cephBucketName = "home";
    private static String cephUrl = "http://0.0.0.0"; // not localhost!
    private static String cephMappedUrl;

    private static String amazonAccessKeyID = readPropOrEnv("AWS_ACCESS_KEY");
    private static String amazonSecretAccessKey = readPropOrEnv("AWS_SECRET_KEY");
    private static String amazonRegion = readPropOrEnv("AWS_REGION", "eu-central-1");
    protected static String amazonBucket = readPropOrEnv("AWS_BUCKET", "adorsys-docusafe");
    private static String amazonUrl = readPropOrEnv("AWS_URL");
    private static String amazonMappedUrl;

    private static GenericContainer minioContainer;
    private static GenericContainer cephContainer;

    private static Path tempDir;
    private static AmazonS3 minio;
    private static AmazonS3 ceph;
    private static AmazonS3 amazonS3;

    private static Supplier<Void> cephStorage;
    private static Supplier<Void> minioStorage;
    private static Supplier<Void> amazonSotrage;

    @BeforeAll
    static void init(@TempDir Path tempDir) {
        log.info("Executing init");
        // TODO fixme
        log.info(""); // for some strange reason, the newline of the previous statement is gone
        WithStorageProvider.tempDir = tempDir;

        minioStorage = Suppliers.memoize(() -> {
            startMinio();
            return null;
        });

        cephStorage = Suppliers.memoize(() -> {
            startCeph();
            return null;
        });

        amazonSotrage = Suppliers.memoize(() -> {
            initS3();
            return null;
        });

        registerMBean();
    }

    @AfterEach
    @SneakyThrows
    void cleanup() {
        log.info("Executing cleanup");
        if (null != tempDir && tempDir.toFile().exists()) {
            FileUtils.cleanDirectory(tempDir.toFile());
        }

        if (null != minio) {
            removeObjectFromS3(minio, minioBucketName, bucketPath);
        }

        if (null != ceph) {
            removeObjectFromS3(ceph, cephBucketName, bucketPath);
        }

        if (null != amazonS3) {
            String[] buckets = amazonBucket.split(",");
            if (buckets.length > 1) {
                Stream.of(buckets).forEach(it -> removeObjectFromS3(amazonS3, it, bucketPath));
            } else {
                removeObjectFromS3(amazonS3, amazonBucket, bucketPath);
            }
        }
    }

    @AfterAll
    static void shutdown() {
        log.info("Stopping containers");
        if (null != minioContainer) {
            minioContainer.stop();
            minioContainer = null;
            minio = null;
        }

        if (null != cephContainer) {
            cephContainer.stop();
            cephContainer = null;
            ceph = null;
        }

        amazonS3 = null;
    }

    @ValueSource
    protected static Stream<StorageDescriptor> allLocalDefaultStorages() {
        return Stream.of(
                fs(),
                minio()
                /* No CEPH here because it is quite slow*/
        ).filter(Objects::nonNull);
    }

    @ValueSource
    protected static Stream<StorageDescriptor> allLocalStorages() {
        return Stream.of(
                fs(),
                minio(),
                cephVersioned()
        ).filter(Objects::nonNull);
    }

    @ValueSource
    protected static Stream<StorageDescriptor> allDefaultStorages() {
        return Stream.of(
                fs(),
                minio(),
                s3()
        ).filter(Objects::nonNull);
    }

    @ValueSource
    protected static Stream<StorageDescriptor> allStorages() {
        return Stream.of(
                fs(),
                minio(),
                cephVersioned(),
                s3()
        ).filter(Objects::nonNull);
    }

    protected static StorageDescriptor fs() {
        return new StorageDescriptor(
                StorageDescriptorName.FILESYSTEM,
                () -> new FileSystemStorageService(new Uri(tempDir.toUri())),
                new Uri(tempDir.toUri()),
                null, null, null,
                tempDir.toString()
        );
    }

    protected static StorageDescriptor minio() {
        return new StorageDescriptor(
                StorageDescriptorName.MINIO,
                () -> {
                    minioStorage.get();
                    return new S3StorageService(minio, minioBucketName, EXECUTOR_SERVICE);
                },
                new Uri("s3://" + minioBucketName + "/" + bucketPath + "/"),
                minioAccessKeyID,
                minioSecretAccessKey,
                minioRegion,
                minioBucketName + "/" + bucketPath
        );
    }

    protected static StorageDescriptor cephVersioned() {
        if (skipCeph()) {
            return null;
        }
        return new StorageDescriptor(
                StorageDescriptorName.CEPH,
                () -> {
                    cephStorage.get();
                    return new S3StorageService(ceph, cephBucketName, EXECUTOR_SERVICE);
                },
                new Uri("s3://" + cephBucketName + "/" + bucketPath + "/"),
                cephAccessKeyID,
                cephSecretAccessKey,
                cephRegion,
                cephBucketName  + "/" + bucketPath
        );
    }

    private static boolean skipCeph() {
        String value = System.getProperty(SKIP_CEPH);
        if (value == null) {
            return false;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        return true;
    }

    protected static Function<String, StorageService> storageServiceByBucket() {
         return bucketName -> new S3StorageService(amazonS3, bucketName, EXECUTOR_SERVICE);
    }

    protected static StorageDescriptor s3() {
        if (null == amazonAccessKeyID) {
            return null;
        }

        return new StorageDescriptor(
                StorageDescriptorName.AMAZON,
                () -> {
                    amazonSotrage.get();
                    return new S3StorageService(amazonS3, amazonBucket, EXECUTOR_SERVICE);
                },
                new Uri("s3://" + amazonBucket + "/" + bucketPath + "/"),
                amazonAccessKeyID,
                amazonSecretAccessKey,
                amazonRegion,
                amazonBucket + "/" + bucketPath
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
        log.info("Initializing S3");
        if (Strings.isNullOrEmpty(amazonAccessKeyID)) {
            return;
        }

        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(amazonAccessKeyID, amazonSecretAccessKey))
                );

        String[] buckets = amazonBucket.split(",");
        if (buckets.length > 1) {
            log.info("Using {} buckets:{}", buckets.length, amazonBucket);
        }

        if (StringUtils.isNullOrEmpty(amazonUrl)) {
            amazonS3ClientBuilder = amazonS3ClientBuilder.withRegion(amazonRegion);
            amazonMappedUrl = "s3://" + buckets[0] + "/" + bucketPath + "/";
        } else {
            amazonS3ClientBuilder = amazonS3ClientBuilder
                    .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(amazonUrl, "US")
                    )
                    .enablePathStyleAccess();
            amazonMappedUrl = "http://" + amazonBucket + "." + amazonUrl;
        }
        amazonS3 = amazonS3ClientBuilder.build();

        log.info("Amazon mapped URL:" + amazonMappedUrl);
    }

    private static void startMinio() {
        log.info("Starting MINIO");
        minioContainer = new GenericContainer("minio/minio")
                .withExposedPorts(9000)
                .withEnv("MINIO_ACCESS_KEY", minioAccessKeyID)
                .withEnv("MINIO_SECRET_KEY", minioSecretAccessKey)
                .withCommand("server /data")
                .waitingFor(Wait.defaultWaitStrategy());

        minioContainer.start();
        Integer mappedPort = minioContainer.getMappedPort(9000);
        minioMappedUrl = minioUrl + ":" + mappedPort;
        log.info("Minio mapped URL:" + minioMappedUrl);
        minio = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(minioMappedUrl, minioRegion)
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

    private static void startCeph() {
        log.info("Starting CEPH");
        cephContainer = new GenericContainer("ceph/daemon")
                .withExposedPorts(8000)
                .withEnv("RGW_FRONTEND_PORT", "8000")
                .withEnv("SREE_PORT", "5000")
                .withEnv("DEBUG", "verbose")
                .withEnv("CEPH_DEMO_UID", "nano")
                .withEnv("MON_IP", "127.0.0.1")
                .withEnv("CEPH_PUBLIC_NETWORK", "0.0.0.0/0")
                .withEnv("CEPH_DAEMON", "demo")
                .withEnv("DEMO_DAEMONS", "mon,mgr,osd,rgw")
                .withEnv("CEPH_DEMO_ACCESS_KEY", cephAccessKeyID)
                .withEnv("CEPH_DEMO_SECRET_KEY", cephSecretAccessKey)
                .withCommand("mkdir -p /etc/ceph && mkdir -p /var/lib/ceph && /entrypoint.sh")
                .waitingFor(Wait.defaultWaitStrategy().withStartupTimeout(Duration.ofSeconds(60)));

        cephContainer.start();
        Integer mappedPort = cephContainer.getMappedPort(8000);
        cephMappedUrl = cephUrl + ":" + mappedPort;
        log.info("Ceph mapped URL:" + cephMappedUrl);
        ceph = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(cephMappedUrl, cephRegion)
                )
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(cephAccessKeyID, cephSecretAccessKey)
                        )
                )
                .enablePathStyleAccess()
                .build();

        ceph.createBucket(cephBucketName);
        // curiously enough CEPH docs are incorrect, looks like they do support version id:
        // https://github.com/ceph/ceph/blame/bc065cae7857c352ca36d5f06cdb5107cf72ed41/src/rgw/rgw_rest_s3.cc
        // so for versioned local tests we can use CEPH
        ceph.setBucketVersioningConfiguration(
                new SetBucketVersioningConfigurationRequest(
                        cephBucketName,
                        new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)
                )
        );
    }

    /**
     * Reads property by {@code name} and if such property doesn't exist then it reads it from environment variables.
     * @param name Property/environment variable name
     * @param defaultValue Default value if none are present
     * @return Property value
     */
    protected static String readPropOrEnv(String name, String defaultValue) {
        String fromEnv = System.getProperty(name, System.getenv(name));
        return null != fromEnv ? fromEnv : defaultValue;
    }

    /**
     * Reads property by {@code name} and if such property doesn't exist then it reads it from environment variables.
     * @param name Property/environment variable name
     * @return Property value
     */
    private static String readPropOrEnv(String name) {
        return readPropOrEnv(name, null);
    }

    @Getter
    @ToString(of = "name")
    @AllArgsConstructor
    public static class StorageDescriptor {

        private final StorageDescriptorName name;
        private final Supplier<StorageService> storageService;
        private final Uri location;
        private final String accessKey;
        private final String secretKey;
        private final String region;
        private final String rootBucket;

        public String getMappedUrl() {
            switch(name) {
                case MINIO: return minioMappedUrl;
                case CEPH: return cephMappedUrl;
                case AMAZON: return amazonMappedUrl;
                case FILESYSTEM: return null;
                default: throw new RuntimeException("missing switch for " + name);
            }
        }
    }

    public enum StorageDescriptorName {
        FILESYSTEM,
        MINIO,
        CEPH,
        AMAZON
    }

    @SneakyThrows
    private static void registerMBean() {
        ObjectName objectName = new ObjectName("de.adorsys.datasafe:type=basic,name=threadPoolStatus");
        ThreadPoolStatus threadPoolStatus = new ThreadPoolStatus((ThreadPoolExecutor) EXECUTOR_SERVICE);
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.registerMBean(threadPoolStatus, objectName);
    }
}
