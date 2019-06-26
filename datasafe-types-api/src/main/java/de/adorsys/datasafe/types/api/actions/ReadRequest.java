package de.adorsys.datasafe.types.api.actions;

import de.adorsys.datasafe.types.api.resource.*;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

/**
 * Request to read data at some location
 * @param <T> Resource owner
 * @param <L> Resource location
 */
@Value
@Builder(toBuilder = true)
public class ReadRequest<T, L extends ResourceLocation> {

    @NonNull
    private final T owner;

    @NonNull
    private final L location;

    public static <T> ReadRequest<T, PrivateResource> forPrivate(T owner, PrivateResource path) {
        return new ReadRequest<>(owner, path);
    }

    public static <T> ReadRequest<T, PrivateResource> forDefaultPrivateWithVersion(
            T owner, String path, Version version) {
        return forDefaultPrivateWithVersion(owner, BasePrivateResource.forPrivate(path), version);
    }

    public static <T> ReadRequest<T, PrivateResource> forDefaultPrivateWithVersion(
            T owner, PrivateResource path, Version version) {
        return new ReadRequest<>(owner, new VersionedPrivateResource<>(path, version));
    }

    public static <T> ReadRequest<T, PrivateResource> forDefaultPrivate(T owner, String path) {
        return forDefaultPrivate(owner, new Uri(path));
    }

    public static <T> ReadRequest<T, PrivateResource> forDefaultPrivate(T owner, URI path) {
        return forDefaultPrivate(owner, new Uri(path));
    }

    public static <T> ReadRequest<T, PrivateResource> forDefaultPrivate(T owner, Uri path) {
        return new ReadRequest<>(owner, BasePrivateResource.forPrivate(path));
    }
}
