package de.adorsys.datasafe.types.api.resource;

/**
 * Interface for resource path, both physical and logical that may contain extra information like file metadata.
 */
public interface ResourceLocation<T> {

    /**
     * @return Resource location, possibly with access credentials.
     */
    Uri location();

    /**
     * Rebases/resolves relative uri - called when resolving relative path against absolute.
     * @param absolute uri to resolve against
     * @return path of resource relative to the absolute uri
     * When calling "/path/to/files/".resolveFrom("s3://bucket/") result will be s3://bucket/path/to/files/
     */
    T resolveFrom(ResourceLocation absolute);
}
