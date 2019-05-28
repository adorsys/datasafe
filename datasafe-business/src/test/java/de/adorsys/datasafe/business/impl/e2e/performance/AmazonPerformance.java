package de.adorsys.datasafe.business.impl.e2e.performance;

import de.adorsys.datasafe.business.impl.e2e.DatasafeServicesProvider;
import de.adorsys.datasafe.business.impl.e2e.performance.services.OperationExecutor;
import de.adorsys.datasafe.business.impl.e2e.performance.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AmazonPerformance extends WithRandomActionPerformance {

    @ParameterizedTest
    @MethodSource("sizesAndLatency")
    @EnabledIfEnvironmentVariable(named = "AMAZON_PERFORMANCE_TEST", matches = "true")
    void testAmazonBucketPerformance(int size, int latency) {
        DelegatingStorageWithDelay service = storageService(s3(), latency);
        DefaultDatasafeServices services = DatasafeServicesProvider.defaultDatasafeServices(
                service, service.getRootLocation());
        initialize(services);
        initUsers(size);

        OperationExecutor executor = new OperationExecutor(
                services.privateService(),
                services.inboxService(),
                users,
                STATS.computeIfAbsent(named("AMAZON BUCKET ", size, latency), id -> new StatisticService())
        );

        executeOperations(executor);
    }
}
