package de.adorsys.datasafe.business.api.types.action;

import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ListRequest<T> {

    @NonNull
    private final T owner;

    @NonNull
    private final PrivateResource location;

    private final boolean recursiveFlag;
}
