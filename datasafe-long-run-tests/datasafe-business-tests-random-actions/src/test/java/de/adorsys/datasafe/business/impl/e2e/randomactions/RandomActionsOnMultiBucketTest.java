package de.adorsys.datasafe.business.impl.e2e.randomactions;

import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.BaseRandomActions;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.storage.api.UserBasedDelegatingStorage;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static de.adorsys.datasafe.business.impl.e2e.randomactions.framework.BaseRandomActions.ENABLE_MULTI_BUCKET_TEST;

@Slf4j
@EnabledIfSystemProperty(named = ENABLE_MULTI_BUCKET_TEST, matches = "true")
class RandomActionsOnMultiBucketTest extends BaseRandomActions {

    @ParameterizedTest
    @MethodSource("actionsOnStoragesAndThreadsAndFilesizes")
    void testRandomActionsParallelThreads(StorageDescriptor descriptor, int threadCount, int filesizeInMb) {
        DefaultDatasafeServices datasafeServices = datasafeServices(descriptor);
        StatisticService statisticService = new StatisticService();

        executeTest(
                getFixture(),
                descriptor.getName(),
                filesizeInMb,
                threadCount,
                datasafeServices.userProfile(),
                datasafeServices.privateService(),
                datasafeServices.inboxService(),
                statisticService
        );
    }

    private DefaultDatasafeServices datasafeServices(StorageDescriptor descriptor) {

        descriptor.getStorageService().get();

        return DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig(descriptor.getLocation(), new ReadStorePassword("PAZZWORT")))
                .storage(new UserBasedDelegatingStorage(storageServiceByBucket(), buckets))
                .build();
    }
}
