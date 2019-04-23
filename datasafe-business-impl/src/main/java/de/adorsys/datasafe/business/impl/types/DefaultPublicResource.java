package de.adorsys.datasafe.business.impl.types;

import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.URI;
import java.util.function.Supplier;

@ToString
@RequiredArgsConstructor
public class DefaultPublicResource implements PublicResource {

    private final URI uri;

    @Override
    public URI locationWithAccess() {
        return uri;
    }

    @Override
    public Supplier<PublicResource> applyRoot(ResourceLocation location) {
        return () -> new DefaultPublicResource(location.locationWithAccess().resolve(uri));
    }
}
