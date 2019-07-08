package de.adorsys.datasafe.types.api.shared;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * This class configures Mockito so that it will inject annotated mocks and display exceptions always
 * at correct places.
 * Tests that use Mockito should use this class.
 */
public abstract class BaseMockitoTest {

    @BeforeEach
    public void setup() {
        System.setProperty("SECURE_LOGS", "off");
        System.setProperty("SECURE_SENSITIVE", "off");
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    @AfterAll
    public static void afterAll() {
        System.setProperty("SECURE_LOGS", "on");
        System.setProperty("SECURE_SENSITIVE", "on");
    }
}
