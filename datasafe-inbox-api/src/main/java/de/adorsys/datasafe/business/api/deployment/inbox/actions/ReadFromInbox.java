package de.adorsys.datasafe.business.api.deployment.inbox.actions;

import de.adorsys.datasafe.business.api.types.inbox.InboxReadRequest;

import java.io.InputStream;

public interface ReadFromInbox {

    InputStream read(InboxReadRequest request);
}
