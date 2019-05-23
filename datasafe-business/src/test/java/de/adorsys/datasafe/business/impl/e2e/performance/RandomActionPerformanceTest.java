package de.adorsys.datasafe.business.impl.e2e.performance;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.ResolvedResource;
import de.adorsys.datasafe.business.impl.e2e.DatasafeServicesProvider;
import de.adorsys.datasafe.business.impl.e2e.WithStorageProvider;
import de.adorsys.datasafe.business.impl.e2e.performance.dto.UserSpec;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.e2e.performance.services.ContentGenerator;
import de.adorsys.datasafe.business.impl.e2e.performance.services.OperationExecutor;
import de.adorsys.datasafe.business.impl.e2e.performance.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This test computes minio versioned and non-versioned performance based on same request fixture.
 */
@Slf4j
class RandomActionPerformanceTest extends WithStorageProvider {

    private static Fixture fixture;

    private static final Map<String, StatisticService> STATS = new HashMap<>();

    private Map<String, UserSpec> users;
    private OperationExecutor executor;
    private VersionedDatasafeServices versionedDatasafeServices;
    private WithStorageProvider.StorageDescriptor descriptor;
    private String testId;

    @BeforeAll
    @SneakyThrows
    static void init() {
        try (Reader reader = Resources.asCharSource(
                Resources.getResource("performance/fixture/fixture.json"),
                StandardCharsets.UTF_8).openStream()) {

            fixture = new Gson().fromJson(reader, Fixture.class);
        }
    }

    @BeforeEach
    void generateDataAndRegisterUsers() {
        this.users = new HashMap<>();
        this.descriptor = minio();
        this.testId = UUID.randomUUID().toString();
    }

    @AfterAll
    static void reportStats() {
        STATS.forEach((name, stat) -> log.info("Perofrmance for <{}>: {}", name, stat.reportAsJson(name)));
    }

    @ParameterizedTest
    @MethodSource("sizesAndLatency")
    @EnabledIfEnvironmentVariable(named = "PERFORMANCE_TEST", matches = "true")
    void testMinioVersionedPerformance(int size, int latency) {
        initServices(descriptor, latency);
        initUsers(size);

        executor = new OperationExecutor(
                versionedDatasafeServices.latestPrivate(),
                versionedDatasafeServices.inboxService(),
                users,
                STATS.computeIfAbsent(named("MINIO VERSIONED ", size, latency), id -> new StatisticService())
        );

        executeOperations();
    }

    @ParameterizedTest
    @MethodSource("sizesAndLatency")
    @EnabledIfEnvironmentVariable(named = "PERFORMANCE_TEST", matches = "true")
    void testMinioNonVersionedPerformance(int size, int latency) {
        initServices(descriptor, latency);
        initUsers(size);

        executor = new OperationExecutor(
                versionedDatasafeServices.privateService(),
                versionedDatasafeServices.inboxService(),
                users,
                STATS.computeIfAbsent(named("MINIO NON-VERSIONED ", size, latency), id -> new StatisticService())
        );

        executeOperations();
    }

    private static Stream<Arguments> sizesAndLatency() {
        return Sets.cartesianProduct(
                ImmutableSet.of(1024, 10240, 102400, 1024000),
                ImmutableSet.of(0, 10, 100, 250, 500)
        ).stream().map(it -> Arguments.of(it.get(0), it.get(1)));
    }

    private void executeOperations() {
        fixture.getOperations().forEach(executor::execute);
    }

    private void initServices(WithStorageProvider.StorageDescriptor descriptor, long latency) {
        this.versionedDatasafeServices =
                DatasafeServicesProvider.versionedDatasafeServices(
                        new DelegatingStorageWithDelay(
                                descriptor.getStorageService().get(),
                                () -> 0 == latency ? 0 : ThreadLocalRandom.current().nextLong(
                                        (long) (latency * 0.8),
                                        (long) (latency * 1.2)
                                )
                        ),
                        descriptor.getLocation()
                );

        initialize(versionedDatasafeServices);
    }

    private void initUsers(int fileSize) {
        fixture.getUserPrivateSpace().forEach((userId, space) -> {
            UserIDAuth auth = registerUser(testId + "-" + userId, descriptor.getLocation());
            users.put(userId, new UserSpec(auth, new ContentGenerator(fileSize)));
        });
    }

    private static String named(String baseName, int size, int latency) {
        return String.format("%s:size=%d latency=%d", baseName, size, latency);
    }

    @RequiredArgsConstructor
    static class DelegatingStorageWithDelay implements StorageService {

        private final StorageService delegate;
        private final Supplier<Long> delayProvider;

        @Override
        @SneakyThrows
        public boolean objectExists(AbsoluteLocation location) {
            Thread.sleep(delayProvider.get());
            return delegate.objectExists(location);
        }

        @Override
        @SneakyThrows
        public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
            Thread.sleep(delayProvider.get());
            return delegate.list(location);
        }

        @Override
        @SneakyThrows
        public InputStream read(AbsoluteLocation location) {
            Thread.sleep(delayProvider.get());
            return delegate.read(location);
        }

        @Override
        @SneakyThrows
        public void remove(AbsoluteLocation location) {
            Thread.sleep(delayProvider.get());
            delegate.remove(location);

        }

        @Override
        @SneakyThrows
        public OutputStream write(AbsoluteLocation location) {
            Thread.sleep(delayProvider.get());
            return delegate.write(location);
        }
    }
}
