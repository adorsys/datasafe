package de.adorsys.datasafe.business.impl.e2e.randomactions;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.BaseRandomActions;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.MultiStorageDelegation;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    private static AmazonS3 directoryClient = null;

    @ParameterizedTest
    @MethodSource("actionsOnMultiStorageAndThreadsAndFilesizes")
    void testRandomActionsMultiStorageParallelThreads(List<StorageDescriptor> descriptors, int threadCount, int filesizeInMb) {

        MultiStorageDelegation multiStorageDelegation = new MultiStorageDelegation(descriptors, smallFixture());
        DefaultDatasafeServices datasafeServices = multiStorageDelegation.getDatasafeServices();

        executeTest(
                smallFixture(),
                descriptors.get(0).getName(),
                filesizeInMb,
                threadCount,
                datasafeServices.userProfile(),
                datasafeServices.privateService(),
                datasafeServices.inboxService(),
                new StatisticService(),
                descriptors
        );
    }
}
