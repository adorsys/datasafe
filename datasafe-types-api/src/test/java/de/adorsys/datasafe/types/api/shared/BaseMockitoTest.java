package de.adorsys.datasafe.types.api.shared;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * This class configures Mockito so that it will inject annotated mocks and display exceptions always
 * at correct places.
 * Tests that use Mockito should use this class.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseMockitoTest {

    @BeforeEach
    public void setup() {
        System.setProperty("SECURE_LOGS", "off");
        System.setProperty("SECURE_SENSITIVE", "off");
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
