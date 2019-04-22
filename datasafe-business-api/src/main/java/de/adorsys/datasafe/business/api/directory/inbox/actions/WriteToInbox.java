package de.adorsys.datasafe.business.api.directory.inbox.actions;

import de.adorsys.datasafe.business.api.types.action.InboxWriteRequest;

import java.io.OutputStream;

public interface WriteToInbox {

    OutputStream write(InboxWriteRequest request);
}
