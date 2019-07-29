package de.adorsys.datasafe.business.impl.e2e.randomactions.framework;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.*;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.OperationExecutor;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.OperationQueue;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
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
import java.util.*;
import java.util.concurrent.*;
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

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int MEGABYTE_TO_BYTE = 1024 * 1024;
    private static final long TIMEOUT = 30L;

    private static final Set<Integer> THREAD_COUNT = ImmutableSet.of(2, 4);
    private static final Set<Integer> FILE_SIZE_M_BYTES = ImmutableSet.of(1, 10);

    @BeforeEach
    void prepare() {
        // Enable logging obfuscation
        System.setProperty("SECURE_LOGS", "on");
        System.setProperty("SECURE_SENSITIVE", "on");
    }

    protected Fixture smallSimpleDocusafeAdapterFixture() {
        return fixture("fixture/fixture_simple_datasafe_200_ops.json");
    }

    protected static Fixture smallFixture() {
        return fixture("fixture/fixture_200_ops.json");
    }

    @SneakyThrows
    protected static Fixture fixture(String path) {
        try (Reader reader = Resources.asCharSource(
                Resources.getResource(path),
                StandardCharsets.UTF_8).openStream()) {
            return new Gson().fromJson(reader, Fixture.class);
        }
    }

    @ValueSource
    protected static Stream<Arguments> actionsOnSoragesAndThreadsAndFilesizes() {
        return Sets.cartesianProduct(
                Collections.singleton(s3()),
            THREAD_COUNT,
            FILE_SIZE_M_BYTES
        ).stream().map(it -> Arguments.of(it.get(0), it.get(1), it.get(2)));
    }

    @ValueSource
    protected static Stream<Arguments> multipleActionsOnSoragesAndThreadsAndFilesizes() {
        return Sets.cartesianProduct(
                Collections.singleton(getS3Bucket()),
                THREAD_COUNT,
                FILE_SIZE_M_BYTES
        ).stream().map(it -> Arguments.of(it.get(0), it.get(1), it.get(2)));
    }

    @ValueSource
    protected static Stream<Arguments> testMultiStorageParallelThreads() {
        return Sets.cartesianProduct(
                Collections.singleton(getS3Bucket()),
                THREAD_COUNT,
                FILE_SIZE_M_BYTES
        ).stream().map(it -> Arguments.of(it.get(0), it.get(1), it.get(2)));
    }

    protected void executeTest(Fixture fixture, List<StorageDescriptor> listDescriptor, int filesizeInMb, int threadCount) {
        List<Operation> noOfUsers = fixture.getUserPrivateSpace().keySet().stream()
                .map(it -> Operation.builder()
                        .type(OperationType.CREATE_USER)
                        .userId(it).build())
                .collect(Collectors.toList());

        String fromEnv = System.getProperty("AWS_S3_BUCKET_COUNT", System.getenv("AWS_S3_BUCKET_COUNT"));
        String amazons3BucketCount = null != fromEnv ? fromEnv : null;

        if(amazons3BucketCount != null){
            int s3BucketCount = Integer.parseInt(amazons3BucketCount);
            if(s3BucketCount > 1){
                for(Operation user : noOfUsers) {
                    UserFixture userFixture = getUserFixture(user, fixture, Integer.parseInt(amazons3BucketCount), listDescriptor, filesizeInMb, threadCount);
                    executeTest(fixture,
                            userFixture.getDescriptor().getName(),
                            filesizeInMb,
                            threadCount,
                            userFixture.getDatasafeServices().userProfile(),
                            userFixture.getDatasafeServices().privateService(),
                            userFixture.getDatasafeServices().inboxService(),
                            userFixture.getStatisticService());
                }
            }else{
                StorageDescriptor descriptor = listDescriptor.get(0);
                DefaultDatasafeServices datasafeServices = datasafeServices(descriptor);
                StatisticService statisticService = new StatisticService();

                executeTest(fixture,
                        descriptor.getName(),
                        filesizeInMb,
                        threadCount,
                        datasafeServices.userProfile(),
                        datasafeServices.privateService(),
                        datasafeServices.inboxService(),
                        statisticService);
            }
        }

    }

    private DefaultDatasafeServices datasafeServices(StorageDescriptor descriptor) {
        return DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(descriptor.getLocation(), "PAZZWORT"))
                .storage(descriptor.getStorageService().get())
                .build();
    }

    private UserFixture getUserFixture(Operation user, Fixture fixture, int s3BucketCount, List<StorageDescriptor> listDescriptor, int filesizeInMb, int threadCount) {

        String[] userName = user.getUserId().split("-");
        int userId = Integer.parseInt(userName[1]);
        int s3BucketPosition = 0;
        if (userId < s3BucketCount) {
            s3BucketPosition = userId;
        } else {
            s3BucketPosition = getS3BucketPosition(userId, s3BucketCount);
        }

        StorageDescriptor descriptor = listDescriptor.get(s3BucketPosition);

        String userId_ = user.getUserId();
        Map<String, Map<String, ContentId>> userPrivateSpace = new HashMap<>();
        Map<String, ContentId> userPrivateSpaceMap = fixture.getUserPrivateSpace().get(userId_);
        userPrivateSpace.put(userId_, userPrivateSpaceMap);

        Map<String, Map<String, ContentId>> userPublicSpace = new HashMap<>();
        Map<String, ContentId> userPublicSpaceMap = fixture.getUserPublicSpace().get(userId_);
        userPublicSpace.put(userId_, userPublicSpaceMap);

        List<Operation> allUserOperations = fixture.getOperations();
        List<Operation> userOperations = allUserOperations.stream()
                .filter(opr->opr.getUserId().equalsIgnoreCase(userId_))
                .collect(Collectors.toList());

        //userOperations.sort((o1, o2) -> o1.getType().compareTo(o2.getType()));

        Fixture fixturebyUser = new Fixture(userOperations, userPrivateSpace, userPublicSpace);

        DefaultDatasafeServices datasafeServices = datasafeServices(descriptor);

        return new UserFixture(fixturebyUser, descriptor, datasafeServices, new StatisticService());
    }

    //Reference - To be removed
    protected void executeTest_TO_REMOVE(Fixture fixture, List<StorageDescriptor> listDescriptor, int filesizeInMb, int threadCount) {
        List<Operation> createUsersOperation = fixture.getUserPrivateSpace().keySet().stream()
                .map(it -> Operation.builder()
                        .type(OperationType.CREATE_USER)
                        .userId(it).build())
                .collect(Collectors.toList());

        String fromEnv = System.getProperty("AWS_S3_BUCKET_COUNT", System.getenv("AWS_S3_BUCKET_COUNT"));
        String amazons3BucketCount = null != fromEnv ? fromEnv : null;

        if(amazons3BucketCount != null){
            Integer s3BucketCount = Integer.parseInt(amazons3BucketCount);

            for(Operation operation : createUsersOperation){
                String[] userName = operation.getUserId().split("-");
                int userId = Integer.parseInt(userName[1]);
                int s3BucketPosition = 0;
                if(userId < s3BucketCount){
                    s3BucketPosition = userId;
                }else{
                    s3BucketPosition = getS3BucketPosition(userId, s3BucketCount);
                }

                StorageDescriptor descriptor = listDescriptor.get(s3BucketPosition);
                DefaultDatasafeServices datasafeServices = DaggerDefaultDatasafeServices.builder()
                        .config(new DefaultDFSConfig(descriptor.getLocation(), "PAZZWORT"))
                        .storage(descriptor.getStorageService().get())
                        .build();
                StatisticService statisticService = new StatisticService();

                //OperationQueue queue = new OperationQueue(fixture);
                OperationExecutor executor = new OperationExecutor(
                        filesizeInMb * MEGABYTE_TO_BYTE,
                        datasafeServices.userProfile(),
                        datasafeServices.privateService(),
                        datasafeServices.inboxService(),
                        new ConcurrentHashMap<>(),
                        statisticService
                );

                //create users
                executor.execute(operation);

                List<Throwable> exceptions = new CopyOnWriteArrayList<>();

                //boolean terminatedOk = runFixtureInMultipleExecutions(fixture, threadCount, queue, executor, exceptions);

                String userId_ = operation.getUserId();
                Map<String, Map<String, ContentId>> userPrivateSpace = new HashMap<>();
                Map<String, ContentId> userPrivateSpaceMap = fixture.getUserPrivateSpace().get(userId_);
                userPrivateSpace.put(userId_, userPrivateSpaceMap);

                Map<String, Map<String, ContentId>> userPublicSpace = new HashMap<>();
                Map<String, ContentId> userPublicSpaceMap = fixture.getUserPublicSpace().get(userId_);
                userPublicSpace.put(userId_, userPublicSpaceMap);

                List<Operation> operations = new ArrayList<>();
                //operations.add(operation);
                operations.add(Operation.builder().userId(operation.getUserId()).type(OperationType.WRITE).build());
                operations.add(Operation.builder().userId(operation.getUserId()).type(OperationType.READ).build());
                operations.add(Operation.builder().userId(operation.getUserId()).type(OperationType.LIST).build());
                operations.add(Operation.builder().userId(operation.getUserId()).type(OperationType.SHARE).build());
                operations.add(Operation.builder().userId(operation.getUserId()).type(OperationType.DELETE).build());

                Fixture fixture1 = new Fixture(operations, userPrivateSpace, userPublicSpace);

                OperationQueue queue = new OperationQueue(fixture1);
                boolean executedOk = runFixtureInMultipleExecutions(fixture1, threadCount, queue, executor, exceptions);

                assertThat(exceptions).isEmpty();
                assertThat(executedOk).isTrue();

                log.info("==== Statistics for {} with {} threads and {} Mb filesize: ====",
                        descriptor.getName(),
                        threadCount,
                        filesizeInMb
                );

                statisticService.generateReport().forEach((name, percentiles) ->
                        log.info("{} : {}", name, percentiles)
                );
            }
        }
    }

    protected void executeTest(
            Fixture fixture,
            StorageDescriptorName storageName,
            int filesizeInMb,
            int threads,
            ProfileRegistrationService profileRegistrationService,
            PrivateSpaceService privateSpaceService,
            InboxService inboxService,
            StatisticService statisticService
    ) {
        OperationQueue queue = new OperationQueue(fixture);
        OperationExecutor executor = new OperationExecutor(
                filesizeInMb * MEGABYTE_TO_BYTE,
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

        log.info("==== Statistics for {} with {} threads and {} Mb filesize: ====",
                storageName,
                threads,
                filesizeInMb
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

    private int getS3BucketPosition(int userId, Integer s3BucketCount) {
        do{
            userId -= s3BucketCount;
        }while (s3BucketCount <= userId);

        return userId;
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
            List<Throwable> exceptions) throws Exception{

        String threadId = execIds.get(ThreadLocalRandom.current().nextInt(execIds.size()));
        if (!blockedExecIds.add(threadId)) {
            return;
        }

        Operation operation = queue.get(threadId);

        if (null != operation) {
            executeOperation(executor, executorService, blockedExecIds, exceptions, threadId, operation);
            log.info(operation.toString());
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
