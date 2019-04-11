package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.inbox.dto.InboxWriteRequest;

public interface WriteToInbox {

    void writeDocumentToInboxOfUser(InboxWriteRequest request);
}
