package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.VersionStrategy;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.Version;
import de.adorsys.datasafe.business.api.types.resource.Versioned;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;

import java.util.stream.Stream;

public interface VersionedPrivateSpaceService<V extends VersionStrategy> extends
        PrivateSpaceService,
        WithVersionStrategy<V> {

    Stream<Versioned<AbsoluteLocation<PrivateResource>, PrivateResource, Version>> listWithDetails(
            ListRequest<UserIDAuth, PrivateResource> request);
}
