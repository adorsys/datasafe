package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.business.impl.e2e.WithStorageProvider;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.spring.annotations.UseDatasafeSpringConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@SpringBootConfiguration
@UseDatasafeSpringConfiguration
public class InjectionTest extends WithStorageProvider {

    @Autowired
    SimpleDatasafeService datasafeService;

    public void testCreateUser() {
        assertThat(datasafeService).isNotNull();
        UserID userid = new UserID("peter");
        ReadKeyPassword password = new ReadKeyPassword("password");
        UserIDAuth userIDAuth = new UserIDAuth(userid, password);
        assertThat(datasafeService.userExists(userid)).isFalse();
        datasafeService.createUser(userIDAuth);
        assertThat(datasafeService.userExists(userid)).isTrue();
        datasafeService.destroyUser(userIDAuth);
    }
}
