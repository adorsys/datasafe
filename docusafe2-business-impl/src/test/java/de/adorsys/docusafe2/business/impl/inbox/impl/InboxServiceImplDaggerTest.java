package de.adorsys.docusafe2.business.impl.inbox.impl;

import de.adorsys.docusafe2.business.impl.inbox.DaggerDefaultInboxService;
import de.adorsys.docusafe2.business.impl.inbox.DefaultInboxService;
import org.junit.jupiter.api.Test;

class InboxServiceImplDaggerTest {

    @Test
    void testDaggerObjectCreation() {
        DefaultInboxService service = DaggerDefaultInboxService.create();

        service.inboxService();
    }
}