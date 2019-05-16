package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.Version;
import de.adorsys.datasafe.business.api.types.resource.Versioned;

import java.util.stream.Stream;

public interface VersionInfoService<V extends Version> {

    /**
     * @param forUser Private space owner
     * @param resource `folder` or `file` for which to list all versions (recurses and joins versions if needed)
     * @return list of resource versions, in form of resource location, logical resource name, version.
     */
    Stream<Versioned<AbsoluteLocation<PrivateResource>, PrivateResource, V>> versionsOf(
            UserIDAuth forUser, PrivateResource resource
    );
}
