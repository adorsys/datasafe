package de.adorsys.datasafe.business.api.inbox;

import de.adorsys.datasafe.business.api.inbox.actions.ListInbox;
import de.adorsys.datasafe.business.api.inbox.actions.ReadFromInbox;
import de.adorsys.datasafe.business.api.inbox.actions.RemoveFromInbox;
import de.adorsys.datasafe.business.api.inbox.actions.WriteToInbox;

public interface InboxService extends ListInbox, ReadFromInbox, WriteToInbox, RemoveFromInbox {
}
