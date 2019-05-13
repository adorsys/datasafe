package de.adorsys.datasafe.business.api.version.types.action;

import de.adorsys.datasafe.business.api.version.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.types.resource.ResourceLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class ListRequest<T, L extends ResourceLocation> {

    @NonNull
    private final T owner;

    @NonNull
    private final L location;

    public static <T> ListRequest<T, PrivateResource> forPrivate(T owner, String path) {
        return new ListRequest<>(owner, DefaultPrivateResource.forPrivate(URI.create(path)));
    }
}
