package de.adorsys.datasafe.business.impl.types;

import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.URI;

@ToString
@RequiredArgsConstructor
public class DefaultPublicResource implements PublicResource {

    private final URI uri;

    @Override
    public URI locationWithAccess() {
        return uri;
    }

    @Override
    public PublicResource resolve(ResourceLocation location) {
        return new DefaultPublicResource(uri.resolve(location.locationWithAccess()));
    }
}
