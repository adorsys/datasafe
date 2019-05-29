package de.adorsys.datasafe.types.api.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * Base class for storage operations output.
 */
@Getter
@RequiredArgsConstructor
public class BaseResolvedResource implements ResolvedResource {

    private final PrivateResource resource;
    private final Instant modifiedAt;

    /**
     * @param resource New root for this resource
     * @return `Rebased` view of this resource - replaces resource path, but preserves its metadata
     */
    @Override
    public ResolvedResource withResource(PrivateResource resource) {
        return new BaseResolvedResource(resource, modifiedAt);
    }

    /**
     * @return Location as URI of associated resource
     */
    @Override
    public Uri location() {
        return resource.location();
    }

    /**
     * @param absolute New container for this resource
     * @return New resource that has {@code absolute} as the container
     */
    @Override
    public ResolvedResource resolveFrom(ResourceLocation absolute) {
        return new BaseResolvedResource(resource.resolveFrom(absolute), modifiedAt);
    }

    /**
     * @return PrivateResource representation of associated resource path.
     */
    @Override
    public PrivateResource asPrivate() {
        return resource;
    }
}
