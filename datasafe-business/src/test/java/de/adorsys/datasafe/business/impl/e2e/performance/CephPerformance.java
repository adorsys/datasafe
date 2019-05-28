package de.adorsys.datasafe.business.impl.e2e.performance;

import de.adorsys.datasafe.business.impl.e2e.DatasafeServicesProvider;
import de.adorsys.datasafe.business.impl.e2e.performance.services.OperationExecutor;
import de.adorsys.datasafe.business.impl.e2e.performance.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CephPerformance extends WithRandomActionPerformance {

    @ParameterizedTest
    @MethodSource("sizesAndLatency")
    @EnabledIfEnvironmentVariable(named = "CEPH_PERFORMANCE_TEST", matches = "true")
    void testCephBucketPerformance(int size, int latency) {
        DelegatingStorageWithDelay service = storageService(ceph(), latency);
        DefaultDatasafeServices services = DatasafeServicesProvider.defaultDatasafeServices(
                service, service.getRootLocation());
        initialize(services);
        initUsers(size);

        OperationExecutor executor = new OperationExecutor(
                services.privateService(),
                services.inboxService(),
                users,
                STATS.computeIfAbsent(named("CEPH ", size, latency), id -> new StatisticService())
        );

        executeOperations(executor);
    }
}
