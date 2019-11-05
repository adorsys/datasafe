package de.adorsys.datasafe.business.impl.e2e.randomactions.framework;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Operation;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.OperationResult;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.OperationType;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.OperationExecutor;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.OperationQueue;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class performs fixturized actions. Each execution (which is serial by design) submits available action
 * into thread pool (submitted action is serial within owning execution but is parallel to actions in other executions)
 * Execution1 [Action1,Action2,Action3] -- Action3 \
 * Execution2 [Action1,Action2,Action3] -- Action1 - Thread pool - Execute [Action3,Action1,Action1] on shared user set
 * Execution3 [Action1,Action2,Action3] -- Action1 /
 */
public abstract class BaseRandomActions extends WithStorageProvider {

    public static final String DISABLE_RANDOM_ACTIONS_TEST = "DISABLE_RANDOM_ACTIONS_TEST";
    public static final String ENABLE_MULTI_BUCKET_TEST = "ENABLE_MULTI_BUCKET_TEST";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int KILOBYTE_TO_BYTE = 1024;
    private static final long TIMEOUT = 30L;

    private static String THREADS = readPropOrEnv("THREADS", "2, 4, 8");
    private static String FILE_SIZES = readPropOrEnv("FILE_SIZES", "100, 1024, 10240"); // in KB

    private static final Set<Integer> THREAD_COUNT = ImmutableSet.copyOf(
            Stream.of(THREADS.split(",")).map(String::trim)
                    .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList())
    );

    private static final Set<Integer> FILE_SIZE_K_BYTES = ImmutableSet.copyOf(
            Stream.of(FILE_SIZES.split(",")).map(String::trim)
                    .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList())
    );

    private static final List<String> STORAGE_PROVIDERS =
            Arrays.asList(readPropOrEnv("STORAGE_PROVIDERS", "MINIO").split(","));

    private static final String FIXTURE_SIZE = readPropOrEnv("FIXTURE_SIZE", "SMALL");

    @BeforeEach
    void prepare() {
        // Enable logging obfuscation
        System.setProperty("SECURE_LOGS", "on");
        System.setProperty("SECURE_SENSITIVE", "on");
    }

    protected Fixture getFixture() {
        switch(FIXTURE_SIZE) {
            case "MEDIUM" : return fixture("fixture/fixture_1000_ops.json");
            case "LARGE" : return fixture("fixture/fixture_10000_ops.json");
            default : return fixture("fixture/fixture_200_ops.json");
        }
    }

    protected Fixture getSimpleDatasafeAdapterFixture() {
        switch(FIXTURE_SIZE) {
            case "MEDIUM" : return fixture("fixture/fixture_simple_datasafe_1000_ops.json");
            case "LARGE" : return fixture("fixture/fixture_simple_datasafe_10000_ops.json");
            default : return fixture("fixture/fixture_simple_datasafe_200_ops.json");
        }
    }

    @SneakyThrows
    protected Fixture fixture(String path) {
        try (Reader reader = Resources.asCharSource(
                Resources.getResource(path),
                StandardCharsets.UTF_8).openStream()) {
            return new Gson().fromJson(reader, Fixture.class);
        }
    }

    @ValueSource
    protected static Stream<Arguments> actionsOnStoragesAndThreadsAndFilesizes() {
        return Sets.cartesianProduct(
                getStorageDescriptors(),
                THREAD_COUNT,
                FILE_SIZE_K_BYTES
        ).stream().map(it -> Arguments.of(it.get(0), it.get(1), it.get(2)));
    }

    private static Set<StorageDescriptor> getStorageDescriptors() {
        return STORAGE_PROVIDERS.stream().map(it -> {
            switch (it) {
                case "AMAZON":
                    return s3();
                case "MINIO":
                    return minio();
                case "CEPH":
                    return cephVersioned();
                case "FILESYSTEM":
                    return fs();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    protected void executeTest(
            Fixture fixture,
            StorageDescriptorName storageName,
            int filesizeInKb,
            int threads,
            ProfileRegistrationService profileRegistrationService,
            PrivateSpaceService privateSpaceService,
            InboxService inboxService,
            StatisticService statisticService
    ) {
        OperationQueue queue = new OperationQueue(fixture);
        OperationExecutor executor = new OperationExecutor(
                filesizeInKb * KILOBYTE_TO_BYTE,
                profileRegistrationService,
                privateSpaceService,
                inboxService,
                new ConcurrentHashMap<>(),
                statisticService
        );

        createUsers(fixture, executor);

        List<Throwable> exceptions = new CopyOnWriteArrayList<>();

        boolean terminatedOk = runFixtureInMultipleExecutions(fixture, threads, queue, executor, exceptions);

        assertThat(exceptions).isEmpty();
        assertThat(terminatedOk).isTrue();

        log.info("==== Statistics for {} with {} threads and {} Kb filesize: ====",
                storageName,
                threads,
                filesizeInKb
        );

        statisticService.generateReport().forEach((name, percentiles) ->
                log.info("{} : {}", name, percentiles)
        );
    }

    @SneakyThrows
    private boolean runFixtureInMultipleExecutions(
            Fixture fixture,
            int threadCount,
            OperationQueue queue,
            OperationExecutor executor,
            List<Throwable> exceptions) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<String> executionIds = IntStream.range(0, threadCount).boxed()
                .map(it -> UUID.randomUUID().toString())
                .collect(Collectors.toList());
        Set<String> blockedExecutionIds = Collections.synchronizedSet(new HashSet<>());

        do {
            executeNextAction(queue, executor, executorService, executionIds, blockedExecutionIds, exceptions);
        } while (!executionIds.isEmpty() && exceptions.isEmpty());

        executorService.shutdown();
        boolean status = executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        if (!status) {
            return false;
        }

        executionIds.forEach(it -> executor.validateUsersStorageContent(
                it,
                fixture.getUserPrivateSpace(),
                fixture.getUserPublicSpace())
        );

        return true;
    }

    private void createUsers(Fixture fixture, OperationExecutor executor) {
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
        if (!blockedExecIds.add(threadId)) {
            return;
        }

        Operation operation = queue.get(threadId);

        if (null != operation) {
            executeOperation(executor, executorService, blockedExecIds, exceptions, threadId, operation);
            return;
        }

        execIds.remove(threadId);
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

    private void executeWithThreadName(
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
            } catch (Exception ex) { // not RuntimeException because Lombok masks them!
                log.error("Thread {} failed exceptionally", threadName, ex);
                exceptions.add(ex);
            } finally {
                blockedThreadIds.remove(threadId);
                Thread.currentThread().setName(oldName);
            }
        });
    }

    // translates fixture based operation to unique operation using user id + execution id as key
    private static Operation executionTaggedOperation(String execId, Operation operation) {
        Operation.OperationBuilder builder = operation.toBuilder();

        builder.location(execId + "/" + (null == operation.getLocation() ? "" : operation.getLocation()));

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
}
