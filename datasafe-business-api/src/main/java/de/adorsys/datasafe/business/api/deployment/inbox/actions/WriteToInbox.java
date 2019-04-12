package de.adorsys.datasafe.business.api.deployment.inbox.actions;

import de.adorsys.datasafe.business.api.deployment.inbox.dto.InboxWriteRequest;

public interface WriteToInbox {

    void write(InboxWriteRequest request);
}
