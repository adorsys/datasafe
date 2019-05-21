package de.adorsys.datasafe.business.api.types.resource;

import java.time.Instant;

public interface ResolvedResource extends ResourceLocation<ResolvedResource> {

    /**
     * @return Associated resource modification date.
     */
    Instant getModifiedAt();

    /**
     * @return Same resource but seen at the different location (i.e. relative -> absolute)
     */
    ResolvedResource withResource(PrivateResource resource);

    /**
     * @return Resource private location.
     */
    PrivateResource asPrivate();
}
