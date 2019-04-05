package de.adorsys.docusafe2.business.impl.inbox.impl;

import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.impl.inbox.DaggerDefaultInboxService;
import de.adorsys.docusafe2.business.impl.inbox.DefaultInboxService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class InboxServiceImplDaggerTest {

    @Mock
    private UserProfileService userProfileService;

    @Test
    void testDaggerObjectCreation() {
        DefaultInboxService service = DaggerDefaultInboxService.builder()
                .userProfile(userProfileService)
                .build();

        service.inboxService();
    }
}