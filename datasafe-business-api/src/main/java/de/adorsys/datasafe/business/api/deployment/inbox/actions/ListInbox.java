package de.adorsys.datasafe.business.api.deployment.inbox.actions;

import de.adorsys.datasafe.business.api.types.InboxBucketPath;
import de.adorsys.datasafe.business.api.types.UserIDAuth;

import java.util.stream.Stream;

public interface ListInbox {

    Stream<InboxBucketPath> list(UserIDAuth forUser);
}
