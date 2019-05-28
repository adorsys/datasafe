package de.adorsys.datasafe.types.api.resource;

public interface Versioned<T, S, V extends Version> {

    /**
     * Returns resource with version
     */
    T absolute();

    /**
     * Returns resource without version
     */
    S stripVersion();


    V getVersion();
}
