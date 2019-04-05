package de.adorsys.docusafe2.business.api.inbox;

import de.adorsys.docusafe2.business.api.inbox.dto.InboxReadRequest;
import de.adorsys.docusafe2.business.api.inbox.dto.InboxWriteRequest;
import de.adorsys.docusafe2.business.api.types.*;

import java.util.stream.Stream;

public interface InboxService {

    Stream<InboxBucketPath> listInbox(UserIdAuth forUser);
    void writeDocumentToInboxOfUser(InboxWriteRequest request);
    void readDocumentFromInbox(InboxReadRequest request);
}
