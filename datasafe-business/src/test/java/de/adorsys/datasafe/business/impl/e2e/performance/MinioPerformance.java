package de.adorsys.datasafe.business.impl.e2e.performance;

import de.adorsys.datasafe.business.impl.e2e.DatasafeServicesProvider;
import de.adorsys.datasafe.business.impl.e2e.performance.services.OperationExecutor;
import de.adorsys.datasafe.business.impl.e2e.performance.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Minio performance and stability test using random action execution (both for versioned and non-versioned).
 */
class MinioPerformance extends WithRandomActionPerformance {

    @ParameterizedTest
    @MethodSource("sizesAndLatency")
    @EnabledIfEnvironmentVariable(named = "MINIO_PERFORMANCE_TEST", matches = "true")
    void testMinioVersionedPerformance(int size, int latency) {
        DelegatingStorageWithDelay service = storageService(minio(), latency);
        VersionedDatasafeServices services = DatasafeServicesProvider.versionedDatasafeServices(
                service, service.getRootLocation());
        initialize(services);
        initUsers(size);

        OperationExecutor executor = new OperationExecutor(
                services.latestPrivate(),
                services.inboxService(),
                users,
                STATS.computeIfAbsent(named("MINIO VERSIONED ", size, latency), id -> new StatisticService())
        );

        executeOperations(executor);
    }

    @ParameterizedTest
    @MethodSource("sizesAndLatency")
    @EnabledIfEnvironmentVariable(named = "MINIO_PERFORMANCE_TEST", matches = "true")
    void testMinioNonVersionedPerformance(int size, int latency) {
        DelegatingStorageWithDelay service = storageService(minio(), latency);
        DefaultDatasafeServices services = DatasafeServicesProvider.defaultDatasafeServices(
                service, service.getRootLocation());
        initialize(services);
        initUsers(size);

        OperationExecutor executor = new OperationExecutor(
                services.privateService(),
                services.inboxService(),
                users,
                STATS.computeIfAbsent(named("MINIO ", size, latency), id -> new StatisticService())
        );

        executeOperations(executor);
    }
}
