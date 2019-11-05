package de.adorsys.datasafe.types.api.actions;

import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Request to write data at some location.
 * @param <T> Resource owner.
 * @param <L> Resource path (either relative or absolute).
 */
@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class WriteRequest<T, L extends ResourceLocation> {

    @NonNull
    private final T owner;

    @NonNull
    private final L location;

    @NonNull
    private final StorageIdentifier storageIdentifier;

    @Singular
    private final List<? extends ResourceWriteCallback> callbacks;

    private WriteRequest(@NonNull T owner, @NonNull L location, List<? extends ResourceWriteCallback> callbacks) {
        this.owner = owner;
        this.location = location;
        this.callbacks = callbacks;
        this.storageIdentifier = StorageIdentifier.DEFAULT;
    }

    public static <T> WriteRequest<T, PrivateResource> forDefaultPrivate(T owner, String path) {
        return new WriteRequest<>(owner, BasePrivateResource.forPrivate(new Uri(path)), new ArrayList<>());
    }

    public static <T> WriteRequest<T, PublicResource> forDefaultPublic(T owner, String path) {
        return new WriteRequest<>(owner, new BasePublicResource(new Uri(path)), new ArrayList<>());
    }

    public static <T> WriteRequest<T, PrivateResource> forDefaultPrivate(T owner, URI path) {
        return forDefaultPrivate(owner, new Uri(path));
    }

    public static <T> WriteRequest<T, PrivateResource> forDefaultPrivate(T owner, Uri path) {
        return new WriteRequest<>(owner, BasePrivateResource.forPrivate(path), new ArrayList<>());
    }

    public static <T> WriteRequest<T, PublicResource> forDefaultPublic(T owner, URI path) {
        return forDefaultPublic(owner, new Uri(path));
    }

    public static <T> WriteRequest<T, PublicResource> forDefaultPublic(T owner, Uri path) {
        return new WriteRequest<>(owner, new BasePublicResource(path), new ArrayList<>());
    }

    public static <T> WriteRequest<T, PrivateResource> forPrivate(T owner, StorageIdentifier storage, String path) {
        return new WriteRequest<>(owner, BasePrivateResource.forPrivate(new Uri(path)), storage, new ArrayList<>());
    }

    public static <T> WriteRequest<T, PrivateResource> forPrivate(T owner, StorageIdentifier storage, URI path) {
        return forPrivate(owner, storage, new Uri(path));
    }

    public static <T> WriteRequest<T, PrivateResource> forPrivate(T owner, StorageIdentifier storage, Uri path) {
        return new WriteRequest<>(owner, BasePrivateResource.forPrivate(path), storage, new ArrayList<>());
    }
}
