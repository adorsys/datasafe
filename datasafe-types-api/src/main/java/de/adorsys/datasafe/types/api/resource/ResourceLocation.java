package de.adorsys.datasafe.types.api.resource;

/**
 * Interface for resource path, both physical and logical that may contain extra information like file metadata.
 */
public interface ResourceLocation<T> {

    /**
     * @return Resource location without any credentials.
     */
    Uri location();

    /**
     * Rebases/resolves relative uri - called when resolving relative path against absolute.
     * @param absolute uri to resolve against
     * @return path of resource relative to the absolute uri
     */
    T resolve(ResourceLocation absolute);
}
