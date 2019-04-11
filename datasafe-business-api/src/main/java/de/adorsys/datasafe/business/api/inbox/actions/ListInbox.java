package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.types.file.FileOnBucket;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

import java.util.stream.Stream;

public interface ListInbox {

    Stream<FileOnBucket> listInbox(UserIdAuth forUser);
}
