package de.adorsys.datasafe.business.impl.inbox.impl;

import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.api.profile.UserProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class InboxServiceImplDaggerTest extends BaseMockitoTest {

    @Mock
    private UserProfileService userProfileService;

    @Test
    void testDaggerObjectCreation() {
       /* InboxService inbox = DaggerDefaultInboxService.builder().build().inboxService();

        inbox.readDocumentFromInbox(null);*/
    }
}