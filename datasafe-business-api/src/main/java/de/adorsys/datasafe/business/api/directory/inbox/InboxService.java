package de.adorsys.datasafe.business.api.directory.inbox;

import de.adorsys.datasafe.business.api.directory.inbox.actions.ListInbox;
import de.adorsys.datasafe.business.api.directory.inbox.actions.ReadFromInbox;
import de.adorsys.datasafe.business.api.directory.inbox.actions.WriteToInbox;

public interface InboxService extends ListInbox, ReadFromInbox, WriteToInbox {
}
