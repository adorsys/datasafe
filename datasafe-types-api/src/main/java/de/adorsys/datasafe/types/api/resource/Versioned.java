package de.adorsys.datasafe.types.api.resource;

/**
 * Interface that identifies object that may have versions.
 * @param <T> Resource identifier with version
 * @param <S> Resource identifier without version
 * @param <V> Version tag
 */
public interface Versioned<T, S, V extends Version> {

    /**
     * @return Resource or path to the resource with version (typically physical resource location)
     */
    T absolute();

    /**
     * @return Resource or path to the resource without version (typically some logical location)
     */
    S stripVersion();


    /**
     * @return Version identifier of the resource
     */
    V getVersion();
}
