package de.adorsys.docusafe2.business.api.inbox;

public interface InboxService {

    void listInbox();
    void writeDocumentToInboxOfUser();
    void readDocumentFromInbox();
}
