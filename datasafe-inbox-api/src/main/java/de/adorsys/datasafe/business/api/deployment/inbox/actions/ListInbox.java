package de.adorsys.datasafe.business.api.deployment.inbox.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;

import java.net.URI;
import java.util.stream.Stream;

public interface ListInbox {

    Stream<URI> list(UserIDAuth forUser);
}
