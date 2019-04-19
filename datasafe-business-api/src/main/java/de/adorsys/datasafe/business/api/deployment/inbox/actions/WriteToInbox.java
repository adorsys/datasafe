package de.adorsys.datasafe.business.api.deployment.inbox.actions;

import de.adorsys.datasafe.business.api.types.inbox.InboxWriteRequest;

import java.io.OutputStream;

public interface WriteToInbox {

    OutputStream write(InboxWriteRequest request);
}
