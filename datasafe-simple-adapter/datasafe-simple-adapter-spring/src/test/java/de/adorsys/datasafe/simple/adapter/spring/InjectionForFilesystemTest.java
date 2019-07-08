package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("filesystem")
public class InjectionForFilesystemTest extends InjectionTest {

    @Autowired
    SimpleDatasafeService datasafeService;


    @Test
    public void plainService() {
        testCreateUser(datasafeService);
    }
}
