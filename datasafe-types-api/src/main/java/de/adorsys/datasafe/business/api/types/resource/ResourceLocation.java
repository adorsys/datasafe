package de.adorsys.datasafe.business.api.types.resource;

import java.net.URI;

/**
 * Interface for concrete class that may contain extra information like file metadata.
 */
public interface ResourceLocation<T> {

    /**
     * @return Resource location without any credentials.
     */
    URI location();

    /**
     * Rebases/resolves relative uri - called when resolving relative path against absolute.
     * @param absolute uri to resolve against
     * @return path of resource relative to the absolute uri
     */
    T applyRoot(ResourceLocation absolute);
}
