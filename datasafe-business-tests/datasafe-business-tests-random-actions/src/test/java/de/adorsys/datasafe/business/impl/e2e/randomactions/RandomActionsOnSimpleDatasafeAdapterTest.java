package de.adorsys.datasafe.business.impl.e2e.randomactions;

import de.adorsys.datasafe.teststorage.WithStorageProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Executes random user actions in multiple threads against SimpleDatasafeAdapter.
 * Set of predefined actions fixture is executed by multiple threads and result is validated.
 * Imitates close-to-production SimpleDatasafeAdapter deployment.
 */
class RandomActionsOnSimpleDatasafeAdapterTest extends WithStorageProvider {

    @BeforeAll
    static void prepare() {
        System.setProperty("SECURE_LOGS", "on");
        System.setProperty("SECURE_SENSITIVE", "on");
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void testRandomActionsParrallelThreads(StorageDescriptor descriptor) {
    }
}
