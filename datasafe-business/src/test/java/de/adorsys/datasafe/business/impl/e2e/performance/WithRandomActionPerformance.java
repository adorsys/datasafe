package de.adorsys.datasafe.business.impl.e2e.performance;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.e2e.WithStorageProvider;
import de.adorsys.datasafe.business.impl.e2e.performance.dto.UserSpec;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.e2e.performance.services.ContentGenerator;
import de.adorsys.datasafe.business.impl.e2e.performance.services.OperationExecutor;
import de.adorsys.datasafe.business.impl.e2e.performance.services.StatisticService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This test computes minio versioned and non-versioned performance based on same request fixture.
 */
@Slf4j
abstract class WithRandomActionPerformance extends WithStorageProvider {

    private static Fixture fixture;

    protected static final Map<String, StatisticService> STATS = new HashMap<>();
    protected Map<String, UserSpec> users;

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
        STATS.forEach((name, stat) -> log.info("Performance for <{}>: {}", name, stat.reportAsJson(name)));
    }

    protected static Stream<Arguments> sizesAndLatency() {
        return Sets.cartesianProduct(
                ImmutableSet.of(1024, 10240, 102400, 1024000),
                ImmutableSet.of(0, 10, 100, 250, 500)
        ).stream().map(it -> Arguments.of(it.get(0), it.get(1)));
    }

    protected void executeOperations(OperationExecutor executor) {
        fixture.getOperations().forEach(executor::execute);
    }

    protected void initUsers(int fileSize) {
        fixture.getUserPrivateSpace().forEach((userId, space) -> {
            UserIDAuth auth = registerUser(testId + "-" + userId, descriptor.getLocation());
            users.put(userId, new UserSpec(auth, new ContentGenerator(fileSize)));
        });
    }

    protected static String named(String baseName, int size, int latency) {
        return String.format("%s:size:%d latency:%d", baseName, size, latency);
    }

    protected DelegatingStorageWithDelay storageService(
            WithStorageProvider.StorageDescriptor descriptor, long latency) {
        return new DelegatingStorageWithDelay(
                descriptor.getLocation(),
                descriptor.getStorageService().get(),
                () -> 0 == latency ? 0 : ThreadLocalRandom.current().nextLong(
                        (long) (latency * 0.8),
                        (long) (latency * 1.2)
                )
        );
    }

    @RequiredArgsConstructor
    static class DelegatingStorageWithDelay implements StorageService {

        @Getter
        private final URI rootLocation;

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
