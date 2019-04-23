package de.adorsys.datasafe.business.api.types.resource;

import java.net.URI;

/**
 * Interface for concrete class that may contain extra information like file metadata.
 */
public interface ResourceLocation<T> {

    /**
     * @return resource location with credentials if necessary.
     */
    URI locationWithAccess();

    /**
     * Resolves relative location.
     * @param location uri to resolve
     * @return path of resource relative to the given url
     */
    T resolve(ResourceLocation location);
}
