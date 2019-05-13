package de.adorsys.datasafe.business.api.version.types.action;

import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.types.resource.ResourceLocation;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

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
}
