package de.adorsys.docusafe2.business.api.inbox.actions;

import de.adorsys.docusafe2.business.api.inbox.dto.InboxWriteRequest;

public interface WriteToInbox {

    void writeDocumentToInboxOfUser(InboxWriteRequest request);
}
