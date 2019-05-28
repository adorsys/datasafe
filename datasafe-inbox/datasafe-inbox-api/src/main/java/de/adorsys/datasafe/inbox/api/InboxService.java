package de.adorsys.datasafe.inbox.api;

import de.adorsys.datasafe.inbox.api.actions.ListInbox;
import de.adorsys.datasafe.inbox.api.actions.ReadFromInbox;
import de.adorsys.datasafe.inbox.api.actions.RemoveFromInbox;
import de.adorsys.datasafe.inbox.api.actions.WriteToInbox;

/**
 * Aggregate view of operations possible with users' INBOX. Users' INBOX - asymmetrically encrypted storage
 * of files that are shared with user.
 */
public interface InboxService extends ListInbox, ReadFromInbox, WriteToInbox, RemoveFromInbox {
}
