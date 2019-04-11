package de.adorsys.datasafe.business.impl.inbox.impl;

import de.adorsys.datasafe.business.api.inbox.InboxService;
import de.adorsys.datasafe.business.api.inbox.actions.ListInbox;
import de.adorsys.datasafe.business.api.inbox.actions.ReadFromInbox;
import de.adorsys.datasafe.business.api.inbox.actions.WriteToInbox;
import lombok.experimental.Delegate;

import javax.inject.Inject;

public class InboxServiceImpl implements InboxService {

    @Delegate
    private final ListInbox listInbox;

    @Delegate
    private final ReadFromInbox readDocumentFromInbox;

    @Delegate
    private final WriteToInbox writeDocumentToInbox;

    @Inject
    public InboxServiceImpl(
            ListInbox listInbox,
            ReadFromInbox readDocumentFromInbox,
            WriteToInbox writeDocumentToInbox) {
        this.listInbox = listInbox;
        this.readDocumentFromInbox = readDocumentFromInbox;
        this.writeDocumentToInbox = writeDocumentToInbox;
    }
}
