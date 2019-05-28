package de.adorsys.datasafe.inbox.impl.actions;

import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.inbox.api.actions.ListInbox;
import de.adorsys.datasafe.inbox.api.actions.ReadFromInbox;
import de.adorsys.datasafe.inbox.api.actions.RemoveFromInbox;
import de.adorsys.datasafe.inbox.api.actions.WriteToInbox;
import lombok.experimental.Delegate;

import javax.inject.Inject;

public class InboxServiceImpl implements InboxService {

    @Delegate
    private final ListInbox listInbox;

    @Delegate
    private final ReadFromInbox readDocumentFromInbox;

    @Delegate
    private final WriteToInbox writeDocumentToInbox;

    @Delegate
    private final RemoveFromInbox removeDocumentFromInbox;

    @Inject
    public InboxServiceImpl(
            ListInbox listInbox,
            ReadFromInbox readDocumentFromInbox,
            WriteToInbox writeDocumentToInbox,
            RemoveFromInbox removeFromInbox) {
        this.listInbox = listInbox;
        this.readDocumentFromInbox = readDocumentFromInbox;
        this.writeDocumentToInbox = writeDocumentToInbox;
        this.removeDocumentFromInbox = removeFromInbox;
    }
}
