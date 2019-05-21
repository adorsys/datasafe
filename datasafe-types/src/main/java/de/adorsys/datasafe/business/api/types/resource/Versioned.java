package de.adorsys.datasafe.business.api.types.resource;

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
