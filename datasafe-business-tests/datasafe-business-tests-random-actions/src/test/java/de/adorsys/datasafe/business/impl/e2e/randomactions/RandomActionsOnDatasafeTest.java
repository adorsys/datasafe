package de.adorsys.datasafe.business.impl.e2e.randomactions;

import com.google.common.base.Strings;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.BaseRandomActions;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.ContentId;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Operation;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.OperationResult;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.OperationType;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.OperationExecutor;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.OperationQueue;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executes random user actions in multiple threads against Datasafe-core.
 * Set of predefined actions fixture is executed by multiple threads and result is validated.
 * Imitates close-to-production Datasafe deployment.
 */
@Slf4j
class RandomActionsOnDatasafeTest extends BaseRandomActions {

    private static final int MEGABYTE_TO_BYTE = 1024 * 1024;

    @BeforeAll
    static void prepare() {
        // FIXME
        System.setProperty("SECURE_LOGS", "on");
        System.setProperty("SECURE_SENSITIVE", "on");
    }

    @ParameterizedTest
    @MethodSource("actionsOnAllSoragesAndThreadsAndFilesizes")
    void testRandomActionsParallelThreads(StorageDescriptor descriptor, int threadCount, int filesizeInMb) {
        DefaultDatasafeServices datasafeServices = datasafeServices(descriptor);
        StatisticService statisticService = new StatisticService();

        OperationQueue queue = new OperationQueue(BaseRandomActions.fixture);
        OperationExecutor executor = new OperationExecutor(
                filesizeInMb * MEGABYTE_TO_BYTE,
                datasafeServices.userProfile(),
                datasafeServices.privateService(),
                datasafeServices.inboxService(),
                new ConcurrentHashMap<>(),
                statisticService
        );

        createUsers(executor);

        List<Throwable> exceptions = new CopyOnWriteArrayList<>();

        boolean terminatedOk = runFixtureInMultipleExecutions(threadCount, queue, executor, exceptions);

        assertThat(exceptions).isEmpty();
        assertThat(terminatedOk).isTrue();
    }

    @SneakyThrows
    private boolean runFixtureInMultipleExecutions(int threadCount,
                                                   OperationQueue queue,
                                                   OperationExecutor executor,
                                                   List<Throwable> exceptions) {

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<String> executionIds = IntStream.range(0, threadCount).boxed()
                .map(it -> UUID.randomUUID().toString())
                .collect(Collectors.toList());
        Set<String> blockedExecutionIds = ConcurrentHashMap.newKeySet();

        do {
            executeNextAction(queue, executor, executorService, executionIds, blockedExecutionIds, exceptions);
        } while (!executionIds.isEmpty() && exceptions.isEmpty());

        executorService.shutdown();
        return executorService.awaitTermination(10L, TimeUnit.SECONDS);
    }

    private void createUsers(OperationExecutor executor) {
        List<Operation> createUsers = fixture.getUserPrivateSpace().keySet().stream()
                .map(it -> Operation.builder().type(OperationType.CREATE_USER).userId(it).build())
                .collect(Collectors.toList());

        createUsers.forEach(executor::execute);
    }

    private void executeNextAction(
            OperationQueue queue,
            OperationExecutor executor,
            ExecutorService executorService,
            List<String> execIds,
            Set<String> blockedExecIds,
            List<Throwable> exceptions) {

        String threadId = execIds.get(ThreadLocalRandom.current().nextInt(execIds.size()));
        if (blockedExecIds.contains(threadId)) {
            return;
        }

        blockedExecIds.add()

        Operation operation = queue.get(threadId);

        if (null != operation) {
            executeOperation(executor, executorService, blockedExecIds, exceptions, threadId, operation);
            return;
        }

        validateResultingStorage(executor, executorService, blockedExecIds, exceptions, threadId);
        execIds.remove(threadId);
    }

    private void validateResultingStorage(OperationExecutor executor, ExecutorService executorService,
                                          Set<String> blockedExecIds, List<Throwable> exceptions, String execId) {
        executeWithThreadName(
                execId,
                execId,
                executorService,
                blockedExecIds,
                exceptions,
                () -> executor.validateUsersStorageContent(
                        remapResultsByExecId(execId, fixture.getUserPrivateSpace()),
                        remapResultsByExecId(execId, fixture.getUserPublicSpace())
                )
        );
    }

    private void executeOperation(OperationExecutor executor,
                                  ExecutorService executorService,
                                  Set<String> blockedExecIds,
                                  List<Throwable> exceptions,
                                  String execId,
                                  Operation operation) {
        executeWithThreadName(
                execId,
                execUserId(execId, operation.getUserId()),
                executorService,
                blockedExecIds,
                exceptions,
                () -> executor.execute(executionTaggedOperation(execId, operation))
        );
    }

    private Map<String, Map<String, ContentId>> remapResultsByExecId(
            String execId,
            Map<String, Map<String, ContentId>> storage) {

        return storage.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                it -> it.getValue().entrySet().stream()
                        .collect(Collectors.toMap(path -> execId + "/" + path.getKey(), Map.Entry::getValue))
                )
        );
    }

    private DefaultDatasafeServices datasafeServices(StorageDescriptor descriptor) {
        return DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(descriptor.getLocation(), "PAZZWORT"))
                .storage(descriptor.getStorageService().get())
                .build();
    }

    // translates fixture based operation to unique operation using user id + execution id as key
    private static Operation executionTaggedOperation(String execId, Operation operation) {
        Operation.OperationBuilder builder = operation.toBuilder();
        if (!Strings.isNullOrEmpty(operation.getLocation())) {
            builder.location(execId + "/" + operation.getLocation());
        }
        if (null != operation.getResult() && null != operation.getResult().getDirContent()) {
            OperationResult.OperationResultBuilder resultBuilder = operation.getResult().toBuilder();
            resultBuilder.dirContent(
                    operation.getResult().getDirContent().stream()
                            .map(it -> execId + "/" + it)
                            .collect(Collectors.toSet())
            );

            builder.result(resultBuilder.build());
        }

        return builder.build();
    }

    private static String execUserId(String execId, String userId) {
        return execId + "--" + userId;
    }

    private static void executeWithThreadName(
            String threadId,
            String threadName,
            ExecutorService service,
            Set<String> blockedThreadIds,
            List<Throwable> exceptions,
            Runnable runnable
    ) {
        service.execute(() -> {
            if (!exceptions.isEmpty()) {
                return;
            }
            blockedThreadIds.add(threadId);
            String oldName = Thread.currentThread().getName();
            Thread.currentThread().setName(threadName);
            try {
                runnable.run();
            } catch (RuntimeException ex) {
                log.error("Thread {} failed exceptionally", threadName, ex);
                exceptions.add(ex);
            } finally {
                blockedThreadIds.remove(threadId);
                Thread.currentThread().setName(oldName);
            }
        });
    }
}
