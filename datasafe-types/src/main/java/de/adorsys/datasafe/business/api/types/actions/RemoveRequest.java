package de.adorsys.datasafe.business.api.types.actions;

import de.adorsys.datasafe.business.api.types.resource.BasePrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@Builder(toBuilder = true)
public class RemoveRequest<T, L extends ResourceLocation> {

    @NonNull
    private final T owner;

    @NonNull
    private final L location;

    public static <T> RemoveRequest<T, PrivateResource> forPrivate(T owner, PrivateResource path) {
        return new RemoveRequest<>(owner, path);
    }

    public static <T> RemoveRequest<T, PrivateResource> forDefaultPrivate(T owner, URI path) {
        return new RemoveRequest<>(owner, BasePrivateResource.forPrivate(path));
    }
}