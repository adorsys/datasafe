package de.adorsys.datasafe.business.api.deployment.inbox.actions;

import de.adorsys.datasafe.business.api.deployment.inbox.dto.InboxReadRequest;

public interface ReadFromInbox {

    void read(InboxReadRequest request);
}
