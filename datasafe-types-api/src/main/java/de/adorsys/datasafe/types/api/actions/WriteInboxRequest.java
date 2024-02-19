package de.adorsys.datasafe.types.api.actions;

import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
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
public class WriteInboxRequest<T, R, L extends ResourceLocation> {

    @NonNull T owner;

    @NonNull R recipients;

    @NonNull L location;

    @NonNull StorageIdentifier storageIdentifier;

    @Singular
    List<? extends ResourceWriteCallback> callbacks;

    private WriteInboxRequest(@NonNull T owner, @NonNull R recipients, @NonNull L location, List<? extends ResourceWriteCallback> callbacks) {
        this.owner = owner;
        this.recipients = recipients;
        this.location = location;
        this.callbacks = callbacks;
        this.storageIdentifier = StorageIdentifier.DEFAULT;
    }

    public static <T, R> WriteInboxRequest<T, R, PublicResource> forDefaultPublic(T owner, R recipients, String path) {
        return new WriteInboxRequest<>(owner, recipients, new BasePublicResource(new Uri(path)), new ArrayList<>());
    }

    public static <T, R> WriteInboxRequest<T, R, PublicResource> forDefaultPublic(T owner, R recipients, Uri path) {
        return new WriteInboxRequest<>(owner, recipients, new BasePublicResource(path), new ArrayList<>());
    }

    public static <T, R> WriteInboxRequest<T, R, PublicResource> forDefaultPublic(T owner, R recipients, URI path) {
        return forDefaultPublic(owner, recipients, new Uri(path));
    }
}
