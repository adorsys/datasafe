package de.adorsys.docusafe2.business.impl.inbox.impl;

import de.adorsys.docusafe2.business.api.inbox.InboxService;
import de.adorsys.docusafe2.business.impl.inbox.actions.ListInboxImpl;
import de.adorsys.docusafe2.business.impl.inbox.actions.ReadFromInboxImpl;
import de.adorsys.docusafe2.business.impl.inbox.actions.WriteToInboxImpl;
import lombok.experimental.Delegate;

import javax.inject.Inject;

public class InboxServiceImpl implements InboxService {

    @Delegate
    private final ListInboxImpl listInbox;

    @Delegate
    private final ReadFromInboxImpl readDocumentFromInbox;

    @Delegate
    private final WriteToInboxImpl writeDocumentToInbox;

    @Inject
    public InboxServiceImpl(
            ListInboxImpl listInbox,
            ReadFromInboxImpl readDocumentFromInbox,
            WriteToInboxImpl writeDocumentToInbox) {
        this.listInbox = listInbox;
        this.readDocumentFromInbox = readDocumentFromInbox;
        this.writeDocumentToInbox = writeDocumentToInbox;
    }
}
