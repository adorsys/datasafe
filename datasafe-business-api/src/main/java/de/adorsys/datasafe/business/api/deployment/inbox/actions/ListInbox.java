package de.adorsys.datasafe.business.api.deployment.inbox.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.file.FileOnBucket;

import java.util.stream.Stream;

public interface ListInbox {

    Stream<FileOnBucket> list(UserIDAuth forUser);
}
