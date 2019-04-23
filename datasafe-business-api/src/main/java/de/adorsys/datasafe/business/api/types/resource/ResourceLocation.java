package de.adorsys.datasafe.business.api.types.resource;

import java.net.URI;
import java.util.function.Supplier;

/**
 * Interface for concrete class that may contain extra information like file metadata.
 */
public interface ResourceLocation<T> {

    /**
     * @return resource location with credentials if necessary.
     */
    URI locationWithAccess();

    /**
     * Resolves relative uri - called when resolving relative path against absolute.
     * @param absolute uri to resolve against
     * @return path of resource relative to the absolute uri
     */
    Supplier<T> applyRoot(ResourceLocation absolute);
}
