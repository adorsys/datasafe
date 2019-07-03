package de.adorsys.datasafe.simple.adapter.spring;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("filesystem")
public class InjectionForFilesystemTest extends InjectionTest {

    @Test
    public void plainService() {
        testCreateUser();
    }
}
