package de.adorsys.datasafe.business.impl.e2e.randomactions;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import dagger.Lazy;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.BaseRandomActions;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.dfs.RegexAccessServiceWithStorageCredentialsImpl;
import de.adorsys.datasafe.storage.api.RegexDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3ClientFactory;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static de.adorsys.datasafe.business.impl.e2e.randomactions.framework.BaseRandomActions.DISABLE_RANDOM_ACTIONS_TEST;

/**
 * Executes random user actions in multiple threads against Datasafe-core.
 * We have action fixture for 10 users, where each user does share,read,write,etc. After one executes
 * actions in this fixture he can validate inbox and private directory content using fixture expectation section.
 * This fixture is duplicated N times and submitted to thread pool, so any thread in pool can pick some action and
 * act independently of others. Thread actions and expectations are prefixed with execution id.
 * Imitates close-to-production Datasafe deployment.
 */
@Slf4j
@DisabledIfSystemProperty(named = DISABLE_RANDOM_ACTIONS_TEST, matches = "true")
class RandomActionsOnDatasafeMultipleStorageTest extends BaseRandomActions {
    private static AmazonS3 directoryClient = null;
    private static ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static StorageDescriptor descriptor;

    @ParameterizedTest
    @MethodSource("actionsOnMultiStorageAndThreadsAndFilesizes")
    void testRandomActionsMultiStorageParallelThreads(List<StorageDescriptor> descriptors, int threadCount, int filesizeInMb) {

        descriptor = descriptors.get(0);
        DefaultDatasafeServices datasafeServices = datasafeServices(descriptors.get(0));
        StatisticService statisticService = new StatisticService();
        directoryClient = AmazonS3ClientBuilder.standard()

                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(descriptor.getAccessKey(), descriptor.getSecretKey()))
                )
                .withRegion(descriptor.getRegion())
                .build();

        OverridesRegistry registry = new BaseOverridesRegistry();

        DefaultDatasafeServices multiDfsDatasafe = DaggerDefaultDatasafeServices
                .builder()
                // This storage service will route requests to proper bucket based on URI content:
                // URI with directoryBucket to `directoryStorage`
                // URI with filesBucketOne will get dynamically generated S3Storage
                // URI with filesBucketTwo will get dynamically generated S3Storage
                .storage(
                        new RegexDelegatingStorage(
                                ImmutableMap.<Pattern, StorageService>builder()
                                        // bind URI that contains `directoryBucket` to directoryStorage
                                        .put(
                                                Pattern.compile("user-+"),
                                                // Dynamically creates S3 client with bucket name equal to host value
                                                new UriBasedAuthStorageService(
                                                        acc -> new S3StorageService(
                                                                S3ClientFactory.getClient(
                                                                        acc.getOnlyHostPart().toString(),
                                                                        acc.getAccessKey(),
                                                                        acc.getSecretKey()
                                                                ),
                                                                // Bucket name is encoded in first path segment
                                                                acc.getBucketName(),
                                                                EXECUTOR
                                                        )
                                                )
                                        ).build()
                        )
                )
                .overridesRegistry(registry)
                .build();
        // Instead of default BucketAccessService we will use service that reads storage access credentials from
        // keystore
        BucketAccessServiceImplRuntimeDelegatable.overrideWith(
                registry, args -> new WithCredentialProvider(args.getStorageKeyStoreOperations())
        );


        executeTest(
                smallFixture(),
                descriptor.getName(),
                filesizeInMb,
                threadCount,
                multiDfsDatasafe.userProfile(),
                multiDfsDatasafe.privateService(),
                multiDfsDatasafe.inboxService(),
                statisticService
        );
    }

    private DefaultDatasafeServices datasafeServices(StorageDescriptor descriptor) {
        return DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(descriptor.getLocation(), "PAZZWORT"))
                .storage(descriptor.getStorageService().get())
                .build();
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
