package de.adorsys.datasafe.types.api.shared;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests that use Mockito should use this class.
 */
public abstract class BaseMockitoTest {

    @BeforeEach
    public void setup() {
        System.setProperty("SECURE_LOGS", "off");
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    @AfterAll
    public static void afterAll() {
        System.setProperty("SECURE_LOGS", "on");
    }
}
