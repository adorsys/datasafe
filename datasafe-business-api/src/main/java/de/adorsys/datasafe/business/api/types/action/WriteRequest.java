package de.adorsys.datasafe.business.api.types.action;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class WriteRequest<T, L> {

    @NonNull
    private final T owner;

    @NonNull
    private final L location;
}
