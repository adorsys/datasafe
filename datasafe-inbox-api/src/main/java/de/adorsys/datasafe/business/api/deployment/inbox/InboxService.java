package de.adorsys.datasafe.business.api.deployment.inbox;

import de.adorsys.datasafe.business.api.deployment.inbox.actions.ListInbox;
import de.adorsys.datasafe.business.api.deployment.inbox.actions.ReadFromInbox;
import de.adorsys.datasafe.business.api.deployment.inbox.actions.WriteToInbox;

public interface InboxService extends ListInbox, ReadFromInbox, WriteToInbox {
}
