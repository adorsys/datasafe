package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.action.ListRequest;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;

import java.util.stream.Stream;

public interface ListInbox {

    Stream<AbsoluteResourceLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> forUser);
}
