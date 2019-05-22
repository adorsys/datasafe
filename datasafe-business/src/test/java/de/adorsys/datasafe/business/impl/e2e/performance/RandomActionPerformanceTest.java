package de.adorsys.datasafe.business.impl.e2e.performance;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.impl.e2e.DatasafeServicesProvider;
import de.adorsys.datasafe.business.impl.e2e.WithStorageProvider;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This test computes minio versioned and non-versioned performance based on same request fixture.
 */
class RandomActionPerformanceTest extends WithStorageProvider {

    private static final int FILE_SIZE = 1024;

    private static Fixture fixture;

    private Map<String, UserSpec> users;
    private OperationExecutor executor;
    private DefaultDatasafeServices versionedDatasafeServices;
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
        WithStorageProvider.StorageDescriptor descriptor = minio();
        initVersioned(descriptor);

        this.testId = UUID.randomUUID().toString();

        fixture.getUserPrivateSpace().forEach((userId, space) -> {
            UserIDAuth auth = registerUser(testId + "-" + userId, descriptor.getLocation());
            users.put(userId, new UserSpec(auth, new ContentGenerator(FILE_SIZE)));
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "PERFORMANCE_TEST", matches = "true")
    void testMinioVersionedPerformance() {
        executor = new OperationExecutor(versionedDatasafeServices.privateService(), users);

        executeOperations();
    }

    private void executeOperations() {
        fixture.getOperations().forEach(executor::execute);
    }

    private void initVersioned(WithStorageProvider.StorageDescriptor descriptor) {
        this.versionedDatasafeServices =
                DatasafeServicesProvider.defaultDatasafeServices(
                        descriptor.getStorageService().get(),
                        descriptor.getLocation()
                );

        initialize(versionedDatasafeServices);
    }
}
