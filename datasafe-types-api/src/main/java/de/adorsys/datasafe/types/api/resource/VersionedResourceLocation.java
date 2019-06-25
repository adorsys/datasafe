package de.adorsys.datasafe.types.api.resource;

public interface VersionedResourceLocation<T, V extends Version> extends ResourceLocation<T> {

    V getVersion();
}
