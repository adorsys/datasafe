package de.adorsys.datasafe.metainfo.version.api.version;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.VersionStrategy;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Version;
import de.adorsys.datasafe.types.api.resource.Versioned;

import java.util.stream.Stream;

/**
 * Provides view of privatespace that aims to work with latest versions.
 * @param <V> Versioning class
 */
public interface VersionedPrivateSpaceService<V extends VersionStrategy> extends
        PrivateSpaceService,
        WithVersionStrategy<V> {

    /**
     * Provides list of all available resource versions with latest one (this one with timestamp)
     * @param request Where to list data
     * @return resource version location, latest resource link, version
     */
    Stream<Versioned<AbsoluteLocation<PrivateResource>, ResolvedResource, Version>> listWithDetails(
            ListRequest<UserIDAuth, PrivateResource> request);
}
