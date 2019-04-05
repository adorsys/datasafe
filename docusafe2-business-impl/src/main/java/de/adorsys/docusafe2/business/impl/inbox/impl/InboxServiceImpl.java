package de.adorsys.docusafe2.business.impl.inbox.impl;

import de.adorsys.docusafe2.business.api.inbox.InboxService;
import de.adorsys.docusafe2.business.impl.inbox.actions.ListInbox;
import de.adorsys.docusafe2.business.impl.inbox.actions.ReadDocumentFromInbox;
import de.adorsys.docusafe2.business.impl.inbox.actions.WriteDocumentToInbox;

import javax.inject.Inject;

public class InboxServiceImpl implements InboxService {

    private final ListInbox listInbox;
    private final ReadDocumentFromInbox readDocumentFromInbox;
    private final WriteDocumentToInbox writeDocumentToInbox;

    @Inject
    public InboxServiceImpl(
            ListInbox listInbox,
            ReadDocumentFromInbox readDocumentFromInbox,
            WriteDocumentToInbox writeDocumentToInbox) {
        this.listInbox = listInbox;
        this.readDocumentFromInbox = readDocumentFromInbox;
        this.writeDocumentToInbox = writeDocumentToInbox;
    }

    @Override
    public void listInbox() {

    }

    @Override
    public void writeDocumentToInboxOfUser() {

    }

    @Override
    public void readDocumentFromInbox() {

    }
}
