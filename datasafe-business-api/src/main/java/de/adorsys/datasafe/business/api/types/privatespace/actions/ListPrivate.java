package de.adorsys.datasafe.business.api.types.privatespace.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import java.util.stream.Stream;

public interface ListPrivate {

    Stream<PrivateResource> list(ListRequest<UserIDAuth> request);
}
