package de.adorsys.datasafe.business.api.version.types.action;

import de.adorsys.datasafe.business.api.version.types.resource.*;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@Builder(toBuilder = true)
public class WriteRequest<T, L extends ResourceLocation> {

    @NonNull
    private final T owner;

    @NonNull
    private final L location;

    public static <T> WriteRequest<T, PrivateResource> forPrivate(T owner, String path) {
        return new WriteRequest<>(owner, DefaultPrivateResource.forPrivate(URI.create(path)));
    }

    public static <T> WriteRequest<T, PublicResource> forPublic(T owner, String path) {
        return new WriteRequest<>(owner, new DefaultPublicResource(URI.create(path)));
    }
}
