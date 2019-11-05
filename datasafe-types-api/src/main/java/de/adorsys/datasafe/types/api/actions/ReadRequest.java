package de.adorsys.datasafe.types.api.actions;

import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.Version;
import de.adorsys.datasafe.types.api.resource.VersionedPrivateResource;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Builder(toBuilder = true)
public class ReadRequest<T, L extends ResourceLocation> {

    @NonNull
    private final T owner;

    @NonNull
    private final L location;

    @NonNull
    private final StorageIdentifier storageIdentifier;

    private ReadRequest(@NonNull T owner, @NonNull L location) {
        this.owner = owner;
        this.location = location;
        this.storageIdentifier = StorageIdentifier.DEFAULT;
    }

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

    public static <T> ReadRequest<T, PrivateResource> forPrivate(T owner, StorageIdentifier storage, String path) {
        return new ReadRequest<>(owner, BasePrivateResource.forPrivate(new Uri(path)), storage);
    }

    public static <T> ReadRequest<T, PrivateResource> forPrivate(T owner, StorageIdentifier storage, URI path) {
        return forPrivate(owner, storage, new Uri(path));
    }

    public static <T> ReadRequest<T, PrivateResource> forPrivate(T owner, StorageIdentifier storage, Uri path) {
        return new ReadRequest<>(owner, BasePrivateResource.forPrivate(path), storage);
    }
}
