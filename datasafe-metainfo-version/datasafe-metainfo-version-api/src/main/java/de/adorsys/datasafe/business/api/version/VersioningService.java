package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.Version;
import de.adorsys.datasafe.business.api.types.resource.Versioned;

import java.util.stream.Stream;

public interface VersioningService {

    /**
     * @param resource `folder` or `file` for which to list all versions (recurses and joins versions if needed)
     * @return list of resource versions
     */
    <V extends Version> Stream<Versioned<AbsoluteResourceLocation<PrivateResource>, PrivateResource, V>> versionsOf(
            PrivateResource resource
    );
}
