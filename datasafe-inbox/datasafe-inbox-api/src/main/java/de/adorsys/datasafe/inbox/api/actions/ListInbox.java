package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;

import java.util.stream.Stream;

/**
 * Lists files shared with user inside his INBOX.
 */
public interface ListInbox {

    /**
     * List files/entries at the location (possibly absolute) within INBOX, specified by {@code request}
     * @param request Where to list entries
     * @return Stream of absolute resource locations, by default location is not encrypted
     */
    Stream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> request);
}
