package de.adorsys.docusafe2.business.api.inbox.actions;

import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

import java.util.stream.Stream;

public interface ListInbox {

    Stream<DFSAccess> listInbox(UserIdAuth forUser);
}
