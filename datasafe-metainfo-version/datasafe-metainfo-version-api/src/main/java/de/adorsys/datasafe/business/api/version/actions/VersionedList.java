package de.adorsys.datasafe.business.api.version.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.VersionStrategy;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.Version;
import de.adorsys.datasafe.business.api.types.resource.Versioned;
import de.adorsys.datasafe.business.api.version.WithVersionStrategy;
import de.adorsys.datasafe.business.impl.privatespace.actions.ListPrivate;

import java.util.stream.Stream;

public interface VersionedList<V extends VersionStrategy> extends ListPrivate, WithVersionStrategy<V> {

    @Override
    Stream<AbsoluteLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> request);

    Stream<Versioned<AbsoluteLocation<PrivateResource>, PrivateResource, Version>> listVersioned(
            ListRequest<UserIDAuth, PrivateResource> request
    );
}
