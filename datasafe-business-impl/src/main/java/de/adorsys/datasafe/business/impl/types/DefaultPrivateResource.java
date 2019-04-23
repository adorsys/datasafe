package de.adorsys.datasafe.business.impl.types;

import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.URI;
import java.util.function.Supplier;

@ToString
@RequiredArgsConstructor
public class DefaultPrivateResource implements PrivateResource {

    public static final DefaultPrivateResource ROOT = new DefaultPrivateResource(URI.create("./"));

    private final URI uri;

    @Override
    public URI locationWithAccess() {
        return uri;
    }

    @Override
    public Supplier<PrivateResource> applyRoot(ResourceLocation location) {
        return () -> new DefaultPrivateResource(location.locationWithAccess().resolve(uri));
    }
}
