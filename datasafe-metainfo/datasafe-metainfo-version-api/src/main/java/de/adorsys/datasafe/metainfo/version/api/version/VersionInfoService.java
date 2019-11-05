package de.adorsys.datasafe.metainfo.version.api.version;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Version;
import de.adorsys.datasafe.types.api.resource.Versioned;

import java.util.stream.Stream;

/**
 * Links resource versions joined with its latest version descriptor.
 * Useful for determining which resources are too old and can be removed.
 * @param <V> Versioning class
 */
public interface VersionInfoService<V extends Version> {

    /**
     * Provides all available versions of resource with its timestamp
     * @param request where to list data
     * @return resource with timestamp, with its latest snapshot location and version
     */
    Stream<Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, V>> versionsOf(
            ListRequest<UserIDAuth, PrivateResource> request
    );

    /**
     * Provides all available versions of resource with its timestamp joined with latest version (to compare time)
     * @param request where to list data
     * @return resource with timestamp, with its latest snapshot location (with timestamp) and version
     */
    Stream<Versioned<AbsoluteLocation<ResolvedResource>, ResolvedResource, V>> listJoinedWithLatest(
            ListRequest<UserIDAuth, PrivateResource> request);
}
