package de.adorsys.datasafe.business.impl.e2e.performance;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.impl.e2e.DatasafeServicesProvider;
import de.adorsys.datasafe.business.impl.e2e.WithStorageProvider;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.Operation;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.OperationType;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This test computes minio versioned and non-versioned performance based on same request fixture.
 */
class RandomActionPerformanceTest extends WithStorageProvider {

    private final Map<OperationType, Consumer<Operation>> HANDLERS = ImmutableMap.of(
            OperationType.WRITE, this::doWrite,
            OperationType.READ, this::doRead,
            OperationType.LIST, this::doList,
            OperationType.DELETE, this::doDelete
    );

    private static Fixture fixture;

    private Map<String, UserIDAuth> users;
    private VersionedDatasafeServices versionedDatasafeServices;
    private Path tempDir;
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
    void generateDataAndRegisterUsers(@TempDir Path tempDir) {
        this.users = new HashMap<>();
        WithStorageProvider.StorageDescriptor descriptor = minio();
        initVersioned(descriptor);

        this.tempDir = tempDir;
        this.testId = UUID.randomUUID().toString();

        fixture.getUserPrivateSpace().forEach((userId, space) -> {
            UserIDAuth auth = registerUser(testId + "-" + userId, descriptor.getLocation());
            users.put(userId, auth);
        });
    }

    @Test
    void testMinioVersionedPerformance() {
        executeOperations();
    }

    private void executeOperations() {
        fixture.getOperations().forEach(it -> HANDLERS.get(it.getType()).accept(it));
    }

    private void initVersioned(WithStorageProvider.StorageDescriptor descriptor) {
        this.versionedDatasafeServices =
                DatasafeServicesProvider.versionedDatasafeServices(
                        descriptor.getStorageService().get(),
                        descriptor.getLocation()
                );

        initialize(versionedDatasafeServices);
    }

    private void doWrite(Operation oper) {
    }

    private void doRead(Operation oper) {
    }

    private void doList(Operation oper) {
    }

    private void doDelete(Operation oper) {
    }
}
