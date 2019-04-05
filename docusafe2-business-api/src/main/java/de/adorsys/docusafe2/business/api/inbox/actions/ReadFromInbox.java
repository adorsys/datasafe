package de.adorsys.docusafe2.business.api.inbox.actions;

import de.adorsys.docusafe2.business.api.inbox.dto.InboxReadRequest;

public interface ReadFromInbox {

    void readDocumentFromInbox(InboxReadRequest request);
}
