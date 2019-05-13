package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.action.ListRequest;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;

import java.util.stream.Stream;

public interface ListPrivate {

    Stream<AbsoluteResourceLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> request);
}
