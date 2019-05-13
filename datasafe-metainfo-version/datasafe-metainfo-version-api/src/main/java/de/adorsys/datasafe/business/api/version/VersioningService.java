package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.version.types.action.Version;
import de.adorsys.datasafe.business.api.version.types.action.VersionedResource;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;

import java.util.stream.Stream;

public interface VersioningService {

    /**
     * @param resource `folder` or `file` for which to list all versions (recurses and joins versions if needed)
     * @return list of resource versions
     */
    <V extends Version> Stream<VersionedResource<V, AbsoluteResourceLocation<PrivateResource>>> versionsOf(
            PrivateResource resource
    );
}
