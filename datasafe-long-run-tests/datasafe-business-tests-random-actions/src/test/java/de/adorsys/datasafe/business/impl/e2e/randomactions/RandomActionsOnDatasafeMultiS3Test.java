package de.adorsys.datasafe.business.impl.e2e.randomactions;

import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.BaseRandomActions;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
class RandomActionsOnDatasafeMultiS3Test extends BaseRandomActions {

    @ParameterizedTest
    @MethodSource("multipleActionsOnSoragesAndThreadsAndFilesizes")
    void testRandomActionsParallelThreads(List<StorageDescriptor> listDescriptor, int threadCount, int filesizeInMb) {
        executeNewway(listDescriptor, threadCount, filesizeInMb);
        /*for(StorageDescriptor descriptor : listDescriptor){
            DefaultDatasafeServices datasafeServices = datasafeServices(descriptor);
            StatisticService statisticService = new StatisticService();
            execute(datasafeServices, statisticService, descriptor, threadCount, filesizeInMb);
            *//*ExecutorService executorService = Executors.newFixedThreadPool(threadCount * listDescriptor.size());
            //ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount * listDescriptor.size());
            ExecuteTest executeTest = new ExecuteTest(datasafeServices, statisticService, descriptor, threadCount, filesizeInMb);
            executorService.execute(executeTest);*//*
        }*/
    }

    private void executeNewway(List<StorageDescriptor> listDescriptor, int threadCount, int filesizeInMb) {
        executeTest(smallFixture(),
                listDescriptor,
                filesizeInMb,
                threadCount
                );
    }

    public void execute(DefaultDatasafeServices datasafeServices, StatisticService statisticService, StorageDescriptor descriptor, int threadCount, int filesizeInMb) {
        executeTest(
                smallFixture(),
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
        return DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(descriptor.getLocation(), "PAZZWORT"))
                .storage(descriptor.getStorageService().get())
                .build();
    }
}
