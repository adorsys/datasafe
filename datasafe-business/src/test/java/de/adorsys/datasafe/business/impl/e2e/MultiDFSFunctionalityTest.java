package de.adorsys.datasafe.business.impl.e2e;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import dagger.Lazy;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.dfs.RegexAccessServiceWithStorageCredentialsImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.RegexDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3ClientFactory;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.shared.AwsClientRetry;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.Streams;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.UnrecoverableKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.datasafe.types.api.shared.DockerUtil.getDockerUri;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This test distributes users' storage access keystore, document encryption keystore,
 * users' private files into buckets that reside on different buckets. Bootstrap knows only how to
 * access `credentialsBucket` that has user profile and his storage access keystore.
 */
@Slf4j
class MultiDFSFunctionalityTest extends BaseMockitoTest {

    private static final String REGION = "eu-central-1";
    private static final String LOCALHOST = getDockerUri("http://127.0.0.1");

    private static final String CREDENTIALS = "credentialsbucket";
    private static final String KEYSTORE = "keystorebucket";
    private static final String FILES_ONE = "filesonebucket";
    private static final String FILES_TWO = "filestwobucket";
    private static final String INBOX = "inboxbucket";

    private static final ExecutorService EXECUTOR = ExecutorServiceUtil
        .submitterExecutesOnStarvationExecutingService(5, 5);

    private static Map<String, GenericContainer> minios = new HashMap<>();
    private static Map<String, String> endpointsByHost = new HashMap<>();
    private static Map<String, String> endpointsByHostNoBucket = new HashMap<>();

    private DefaultDatasafeServices datasafeServices;

    @BeforeAll
    static void initDistributedMinios() {
        // Create all required minio-backed S3 buckets:
        Stream.of(CREDENTIALS, KEYSTORE, FILES_ONE, FILES_TWO, INBOX).forEach(it -> {
            GenericContainer minio = new GenericContainer("minio/minio")
                .withExposedPorts(9000)
                .withEnv("MINIO_ACCESS_KEY", accessKey(it))
                .withEnv("MINIO_SECRET_KEY", secretKey(it))
                .withCommand("server /data")
                .waitingFor(Wait.defaultWaitStrategy());
            minio.start();
            minios.put(it, minio);

            String endpoint = LOCALHOST + ":" + minio.getFirstMappedPort() + "/";
            log.info("Minio `{}` with endpoint `{}` and keys `{}`/`{}` has started",
                it, endpoint, accessKey(it), secretKey(it));

            // http://localhost:1234/eu-central-1/bucket/
            endpointsByHost.put(it, endpoint + REGION + "/" + it + "/");
            log.info("ENDPOINT IS {}", endpoint);
            endpointsByHostNoBucket.put(it, endpoint);

            AmazonS3 client = S3ClientFactory.getClient(
                endpoint,
                REGION,
                accessKey(it),
                secretKey(it)
            );

            AwsClientRetry.createBucketWithRetry(client, it);
        });
    }

    @AfterAll
    static void stopAll() {
        minios.forEach((id, minio) -> minio.stop());
    }

    @BeforeEach
    void initDatasafe() {
        StorageService directoryStorage = new S3StorageService(
            S3ClientFactory.getClient(
                endpointsByHostNoBucket.get(CREDENTIALS),
                REGION,
                accessKey(CREDENTIALS),
                secretKey(CREDENTIALS)
            ),
            CREDENTIALS,
            EXECUTOR
        );

        OverridesRegistry registry = new BaseOverridesRegistry();
        this.datasafeServices = DaggerDefaultDatasafeServices.builder()
            .config(new DefaultDFSConfig(endpointsByHost.get(CREDENTIALS), new ReadStorePassword("PAZZWORT")))
            .overridesRegistry(registry)
            .storage(new RegexDelegatingStorage(
                ImmutableMap.<Pattern, StorageService>builder()
                    .put(Pattern.compile(endpointsByHost.get(CREDENTIALS) + ".+"), directoryStorage)
                    .put(
                        Pattern.compile(LOCALHOST + ".+"),
                        new UriBasedAuthStorageService(
                            acc -> new S3StorageService(
                                S3ClientFactory.getClient(
                                    acc.getEndpoint(),
                                    acc.getRegion(),
                                    acc.getAccessKey(),
                                    acc.getSecretKey()
                                ),
                                acc.getBucketName(),
                                EXECUTOR
                            )
                        )
                    ).build())
            ).build();

        BucketAccessServiceImplRuntimeDelegatable.overrideWith(
            registry, args -> new WithCredentialProvider(args.getStorageKeyStoreOperations())
        );
    }

    @Test
    void testWriteToPrivateListPrivateReadPrivate() {
        UserIDAuth john = new UserIDAuth("john", ReadKeyPasswordTestFactory.getForString("my-passwd"));
        registerUser(john);

        validateBasicOperationsAndContent(john);

        deregisterAndValidateEmpty(john);
    }

    @Test
    void testWriteToPrivateListPrivateReadPrivateWithPasswordChange() {
        UserIDAuth john = new UserIDAuth("john", ReadKeyPasswordTestFactory.getForString("my-passwd"));
        registerUser(john);

        validateBasicOperationsAndContent(john);

        ReadKeyPassword newPasswd = ReadKeyPasswordTestFactory.getForString("ANOTHER");
        datasafeServices.userProfile().updateReadKeyPassword(john, newPasswd);
        UserIDAuth newJohn = new UserIDAuth("john", newPasswd);

        assertThrows(UnrecoverableKeyException.class, () -> doBasicOperations(john));
        validateBasicOperationsAndContent(newJohn);

        deregisterAndValidateEmpty(newJohn);
    }

    private void doBasicOperations(UserIDAuth john) {
        writeToPrivate(john, id(FILES_ONE), "path/to/file1.txt", "Hello 1");
        writeToPrivate(john, id(FILES_TWO), "path/to/file2.txt", "Hello 2");

        AbsoluteLocation<ResolvedResource> fileOne = getFirstFileInPrivate(john, id(FILES_ONE), "path/to/file1.txt");
        AbsoluteLocation<ResolvedResource> fileTwo = getFirstFileInPrivate(john, id(FILES_TWO), "path/to/file2.txt");

        assertThat(readFromPrivate(john, fileOne)).isEqualTo("Hello 1");
        assertThat(readFromPrivate(john, fileTwo)).isEqualTo("Hello 2");

        assertThat(readFromPrivate(john, id(FILES_ONE), "path/to/file1.txt")).isEqualTo("Hello 1");
        assertThat(readFromPrivate(john, id(FILES_TWO), "path/to/file2.txt")).isEqualTo("Hello 2");
    }

    private void validateBasicOperationsAndContent(UserIDAuth john) {
        doBasicOperations(john);

        // Validate physical contents
        assertThat(listInBucket(FILES_ONE)).hasSize(1);
        assertThat(listInBucket(FILES_TWO)).hasSize(1);
        assertThat(listInBucket(KEYSTORE)).hasSize(1);
        assertThat(listInBucket(CREDENTIALS)).containsExactlyInAnyOrder(
            "profiles/private/john",
            "profiles/public/john",
            "pubkeys",
            "storagecreds");
    }

    private void deregisterAndValidateEmpty(UserIDAuth john) {
        datasafeServices.userProfile().deregister(john);

        // Validate physical contents
        assertThat(listInBucket(FILES_ONE)).isEmpty();
        assertThat(listInBucket(FILES_TWO)).isEmpty();
        assertThat(listInBucket(KEYSTORE)).isEmpty();
        assertThat(listInBucket(CREDENTIALS)).isEmpty();
    }

    private void registerUser(UserIDAuth auth) {
        String inboxLocation = endpointsByHost.get(INBOX) + "inbox/";
        String pubKeysLocation = endpointsByHost.get(CREDENTIALS) + "pubkeys";

        // User does not declare his keystore location, so we are registering his stuff separately
        datasafeServices.userProfile().registerPublic(CreateUserPublicProfile.builder()
            .id(auth.getUserID())
            .inbox(BasePublicResource.forAbsolutePublic(inboxLocation))
            .publicKeys(BasePublicResource.forAbsolutePublic(pubKeysLocation))
            .build()
        );

        datasafeServices.userProfile().registerPrivate(CreateUserPrivateProfile.builder()
            .id(auth)
            .storageCredentialsKeystore(
                BasePrivateResource.forAbsolutePrivate(endpointsByHost.get(CREDENTIALS) + "storagecreds")
            )
            .inboxWithWriteAccess(BasePrivateResource.forAbsolutePrivate(inboxLocation))
            .keystore(
                BasePrivateResource.forAbsolutePrivate(endpointsByHost.get(KEYSTORE) + "keystore")
            )
            // filesOneBucket is default private space, it is not directly accessible without credentials
            .privateStorage(
                BasePrivateResource.forAbsolutePrivate(endpointsByHost.get(FILES_ONE) + "private/")
            )
            .associatedResources(Collections.emptyList())
            .publishPubKeysTo(BasePublicResource.forAbsolutePublic(pubKeysLocation))
            .build()
        );

        datasafeServices.userProfile().createStorageKeystore(auth);
        // Register users' access to all buckets except `credentialsBucket` that is bootstrap accessible
        Stream.of(KEYSTORE, FILES_ONE, FILES_TWO, INBOX).forEach(it -> {
            String endpoint = endpointsByHost.get(it);
            UserPrivateProfile profile = datasafeServices.userProfile().privateProfile(auth);
            profile.getPrivateStorage().put(
                id(it),
                new AbsoluteLocation<>(BasePrivateResource.forPrivate(endpoint + "/"))
            );

            datasafeServices.userProfile().registerStorageCredentials(
                auth,
                id(it),
                new StorageCredentials(accessKey(it), secretKey(it))
            );

            datasafeServices.userProfile().updatePrivateProfile(auth, profile);
        });

        // finish user registration by creating his keystore
        UserPrivateProfile profile = datasafeServices.userProfile().privateProfile(auth);
        datasafeServices.userProfile().createDocumentKeystore(auth, profile);
    }

    private List<String> listInBucket(String bucket) {
        return S3ClientFactory.getClient(
            endpointsByHostNoBucket.get(bucket),
            REGION,
            accessKey(bucket),
            secretKey(bucket)
        )
            .listObjects(bucket, "")
            .getObjectSummaries()
            .stream()
            .map(S3ObjectSummary::getKey)
            .collect(Collectors.toList());
    }

    @SneakyThrows
    private void writeToPrivate(UserIDAuth user, StorageIdentifier id, String path, String data) {
        try (OutputStream os = datasafeServices.privateService().write(WriteRequest.forPrivate(user, id, path))) {
            os.write(data.getBytes());
        }
    }

    @SneakyThrows
    private String readFromPrivate(UserIDAuth user, StorageIdentifier id, String path) {
        try (InputStream is = datasafeServices.privateService().read(ReadRequest.forPrivate(user, id, path))) {
            return new String(Streams.readAll(is));
        }
    }

    @SneakyThrows
    private String readFromPrivate(UserIDAuth user, AbsoluteLocation<ResolvedResource> location) {
        try (InputStream is = datasafeServices.privateService().read(
            ReadRequest.forPrivate(user, location.getResource().asPrivate()))) {
            return new String(Streams.readAll(is));
        }
    }

    @SneakyThrows
    private AbsoluteLocation<ResolvedResource> getFirstFileInPrivate(UserIDAuth user, StorageIdentifier id,
                                                                     String path) {
        return datasafeServices.privateService()
            .list(ListRequest.forPrivate(user, id, path))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Not found"));
    }

    private static StorageIdentifier id(String endpointId) {
        String endpoint = endpointsByHost.get(endpointId);
        return new StorageIdentifier(endpoint + ".+");
    }

    private static String accessKey(String bucket) {
        return "ACCESS-" + bucket;
    }

    private static String secretKey(String bucket) {
        return "SECRET-" + bucket;
    }

    private static class WithCredentialProvider extends BucketAccessServiceImpl {

        @Delegate
        private final RegexAccessServiceWithStorageCredentialsImpl delegate;

        private WithCredentialProvider(Lazy<StorageKeyStoreOperations> storageKeyStoreOperations) {
            super(null);
            this.delegate = new RegexAccessServiceWithStorageCredentialsImpl(storageKeyStoreOperations);
        }
    }
}
