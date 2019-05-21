package de.adorsys.datasafe.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.validateMockitoUsage;

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
        validateMockitoUsage();
    }
}
