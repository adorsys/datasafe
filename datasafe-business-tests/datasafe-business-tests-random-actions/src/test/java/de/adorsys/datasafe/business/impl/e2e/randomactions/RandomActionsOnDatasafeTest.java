package de.adorsys.datasafe.business.impl.e2e.randomactions;

import de.adorsys.datasafe.teststorage.WithStorageProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Imitates close-to-production Datasafe deployment.
 */
class RandomActionsOnDatasafeTest extends WithStorageProvider {

    @BeforeAll
    static void prepare() {
        System.setProperty("SECURE_LOGS", "on");
        System.setProperty("SECURE_SENSITIVE", "on");
    }

    @ParameterizedTest
    @MethodSource("allStorages")
    void testRandomActionsParrallelThreads(WithStorageProvider.StorageDescriptor descriptor) {
    }
}
