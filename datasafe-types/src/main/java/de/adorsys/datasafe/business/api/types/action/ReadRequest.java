package de.adorsys.datasafe.business.api.types.action;

import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ReadRequest<T> {

    @NonNull
    private final T owner;

    @NonNull
    private final PrivateResource location;

    public ReadRequest(T owner, PrivateResource path) {
        this.owner = owner;
        this.location = path;
    }
}
