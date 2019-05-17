package de.adorsys.datasafe.business.api.version.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.VersionStrategy;
import de.adorsys.datasafe.business.api.types.resource.*;
import de.adorsys.datasafe.business.api.version.WithVersionStrategy;
import de.adorsys.datasafe.business.impl.privatespace.actions.ListPrivate;

import java.util.stream.Stream;

/**
 * Service to list latest filesystem view.
 * @param <V> Versioning class.
 */
public interface VersionedList<V extends VersionStrategy> extends ListPrivate, WithVersionStrategy<V> {

    /**
     * Lists latest resource locations, their date will correspond to latest one, not actual write date
     */
    @Override
    Stream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> request);

    /**
     * Lists versions of resource and provides latest resource link location.
     */
    Stream<Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version>> listVersioned(
            ListRequest<UserIDAuth, PrivateResource> request
    );
}
