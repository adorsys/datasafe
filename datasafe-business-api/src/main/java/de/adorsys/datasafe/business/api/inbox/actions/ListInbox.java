package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.datasafe.business.api.types.UserIdAuth;
import de.adorsys.datasafe.business.api.types.file.FileOnBucket;

import java.util.stream.Stream;

public interface ListInbox {

    Stream<FileOnBucket> listInbox(UserIdAuth forUser);
}
