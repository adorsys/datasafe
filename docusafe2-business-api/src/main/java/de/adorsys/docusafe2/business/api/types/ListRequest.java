package de.adorsys.docusafe2.business.api.types;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ListRequest {

    @NonNull
    private final DFSAccess location;
}
