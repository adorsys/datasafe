package de.adorsys.docusafe2.business.api.inbox;

import de.adorsys.docusafe2.business.api.inbox.actions.ListInbox;
import de.adorsys.docusafe2.business.api.inbox.actions.ReadFromInbox;
import de.adorsys.docusafe2.business.api.inbox.actions.WriteToInbox;

public interface InboxService extends ListInbox, ReadFromInbox, WriteToInbox {
}
