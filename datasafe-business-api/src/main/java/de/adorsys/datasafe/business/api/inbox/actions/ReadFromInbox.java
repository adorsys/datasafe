package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.inbox.dto.InboxReadRequest;

public interface ReadFromInbox {

    void readDocumentFromInbox(InboxReadRequest request);
}
