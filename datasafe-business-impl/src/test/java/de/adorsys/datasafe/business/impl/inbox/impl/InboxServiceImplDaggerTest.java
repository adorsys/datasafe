package de.adorsys.datasafe.business.impl.inbox.impl;

import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeService;
import de.adorsys.datasafe.business.impl.service.DefaultDocusafeService;
import org.junit.jupiter.api.Test;

class InboxServiceImplDaggerTest extends BaseMockitoTest {

    private DefaultDocusafeService docusafeService = DaggerDefaultDocusafeService
            .builder()
            .build();

    @Test
    void testDaggerObjectCreation() {
        UserIDAuth john = registerUser("John");
        UserIDAuth jane = registerUser("Jane");

        docusafeService.inboxService().list(john);
    }

    private UserIDAuth registerUser(String userName) {
        UserIDAuth auth = new UserIDAuth();
        auth.setUserID(new UserID(userName));
        auth.setReadKeyPassword(new ReadKeyPassword("secure-password " + userName));

        docusafeService.userProfile().register(auth);

        return auth;
    }
}