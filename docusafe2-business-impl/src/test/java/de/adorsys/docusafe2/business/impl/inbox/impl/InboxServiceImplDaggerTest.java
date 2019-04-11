package de.adorsys.docusafe2.business.impl.inbox.impl;

import de.adorsys.docusafe2.business.api.inbox.InboxService;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.impl.BaseMockitoTest;
import de.adorsys.docusafe2.business.impl.inbox.DaggerDefaultInboxService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class InboxServiceImplDaggerTest extends BaseMockitoTest {

    @Mock
    private UserProfileService userProfileService;

    @Test
    void testDaggerObjectCreation() {
        InboxService inbox = DaggerDefaultInboxService.builder().build().inboxService();

        inbox.readDocumentFromInbox(null);
    }
}