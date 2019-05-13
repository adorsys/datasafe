package de.adorsys.datasafe.business.api.version.types.resource;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.URI;

@ToString
@RequiredArgsConstructor
public class DefaultPublicResource implements PublicResource {

    private final URI uri;

    @Override
    public URI location() {
        return uri;
    }

    @Override
    public PublicResource resolve(ResourceLocation location) {
        return new DefaultPublicResource(location.location().resolve(uri));
    }
}
