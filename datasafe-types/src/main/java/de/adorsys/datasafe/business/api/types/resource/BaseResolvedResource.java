package de.adorsys.datasafe.business.api.types.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class BaseResolvedResource implements ResolvedResource {

    private final PrivateResource resource;
    private final Instant modifiedAt;


    @Override
    public ResolvedResource withResource(PrivateResource resource) {
        return new BaseResolvedResource(resource, modifiedAt);
    }

    @Override
    public URI location() {
        return resource.location();
    }

    @Override
    public ResolvedResource resolve(ResourceLocation absolute) {
        return new BaseResolvedResource(resource.resolve(absolute), modifiedAt);
    }

    @Override
    public PrivateResource asPrivate() {
        return resource;
    }
}
