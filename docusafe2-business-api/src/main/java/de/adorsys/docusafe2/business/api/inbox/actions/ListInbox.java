package de.adorsys.docusafe2.business.api.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;
import de.adorsys.docusafe2.business.api.types.file.FileOnBucket;

import java.util.stream.Stream;

public interface ListInbox {

    Stream<FileOnBucket> listInbox(UserIdAuth forUser);
}
