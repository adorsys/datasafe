package de.adorsys.datasafe.business.impl.inbox.impl;

import de.adorsys.datasafe.business.api.inbox.InboxService;
import de.adorsys.datasafe.business.api.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.inbox.DaggerDefaultInboxService;
import de.adorsys.datasafe.business.impl.profile.DaggerDefaultProfileService;
import de.adorsys.datasafe.business.impl.profile.filesystem.HashMapProfileStorageImpl;
import org.junit.jupiter.api.Test;

class InboxServiceImplDaggerTest extends BaseMockitoTest {

    private InboxService inbox = DaggerDefaultInboxService.builder().build().inboxService();
    private HashMapProfileStorageImpl creationService = DaggerDefaultProfileService.builder().build().userProfileRegistration();

    @Test
    void testDaggerObjectCreation() {
        UserIDAuth user = registerUser();

        inbox.listInbox(null);
    }

    private UserIDAuth registerUser() {
        UserIDAuth auth = new UserIDAuth();
        auth.setUserID(new UserID("John Doe"));
        auth.setReadKeyPassword(new ReadKeyPassword("secure-password"));

        creationService.register(auth);

        return auth;
    }
}