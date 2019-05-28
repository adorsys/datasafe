package de.adorsys.datasafe.types.api.resource;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.URI;

/**
 * Base class for public (shareable) resource.
 */
@ToString
@RequiredArgsConstructor
public class BasePublicResource implements PublicResource {

    private final URI uri;

    public static AbsoluteLocation<PublicResource> forAbsolutePublic(URI path) {
        return new AbsoluteLocation<>(new BasePublicResource(path));
    }

    @Override
    public URI location() {
        return uri;
    }

    @Override
    public PublicResource resolve(ResourceLocation location) {
        return new BasePublicResource(location.location().resolve(uri));
    }
}
