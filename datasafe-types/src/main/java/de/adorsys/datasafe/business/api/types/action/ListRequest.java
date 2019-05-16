package de.adorsys.datasafe.business.api.types.action;

import de.adorsys.datasafe.business.api.types.resource.BasePrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class ListRequest<T, L extends ResourceLocation> {

    //TODO: Add ROOT bucket path constant
    @NonNull
    private final T owner;

    @NonNull
    private final L location;

    public static <T> ListRequest<T, PrivateResource> forDefaultPrivate(T owner, String path) {
        return new ListRequest<>(owner, BasePrivateResource.forPrivate(URI.create(path)));
    }
}
