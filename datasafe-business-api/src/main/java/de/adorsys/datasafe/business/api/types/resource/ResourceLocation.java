package de.adorsys.datasafe.business.api.types.resource;

import java.net.URI;

/**
 * Interface for concrete class that may contain extra information like file metadata.
 */
public interface ResourceLocation {

    /**
     * @return resource location with credentials if necessary.
     */
    URI locationWithAccess();
}
