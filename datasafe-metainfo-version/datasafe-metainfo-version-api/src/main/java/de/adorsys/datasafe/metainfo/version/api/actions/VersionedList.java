package de.adorsys.datasafe.metainfo.version.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.metainfo.version.api.version.WithVersionStrategy;
import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.VersionStrategy;
import de.adorsys.datasafe.types.api.resource.*;

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
