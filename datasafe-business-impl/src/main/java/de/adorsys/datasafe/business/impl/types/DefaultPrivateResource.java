package de.adorsys.datasafe.business.impl.types;

import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.URI;

@ToString
@RequiredArgsConstructor
public class DefaultPrivateResource implements PrivateResource {

    private final URI uri;

    @Override
    public URI locationWithAccess() {
        return uri;
    }

    @Override
    public PrivateResource resolve(ResourceLocation location) {
        return new DefaultPrivateResource(uri.resolve(location.locationWithAccess()));
    }
}
