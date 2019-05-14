package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import java.util.stream.Stream;

public interface ListPrivate {

    Stream<AbsoluteResourceLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> request);
}
