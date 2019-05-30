package de.adorsys.datasafe.types.api.resource;

import java.time.Instant;

/**
 * Interface used as output from operations with storage. It represents resource path with some
 * essential metadata.
 */
public interface ResolvedResource extends ResourceLocation<ResolvedResource> {

    /**
     * @return Associated resource modification date.
     */
    Instant getModifiedAt();

    /**
     * @param resource New path for this resource
     * @return Relocated resource (example: relative to absolute) that preserves metadata, but drops path.
     */
    ResolvedResource withResource(PrivateResource resource);

    /**
     * @return Resource private location, simply path to the resource without metadata.
     */
    PrivateResource asPrivate();
}
