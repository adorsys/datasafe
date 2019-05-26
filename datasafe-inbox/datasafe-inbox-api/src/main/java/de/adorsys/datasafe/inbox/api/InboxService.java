package de.adorsys.datasafe.inbox.api;

import de.adorsys.datasafe.inbox.api.actions.ListInbox;
import de.adorsys.datasafe.inbox.api.actions.ReadFromInbox;
import de.adorsys.datasafe.inbox.api.actions.RemoveFromInbox;
import de.adorsys.datasafe.inbox.api.actions.WriteToInbox;

public interface InboxService extends ListInbox, ReadFromInbox, WriteToInbox, RemoveFromInbox {
}
