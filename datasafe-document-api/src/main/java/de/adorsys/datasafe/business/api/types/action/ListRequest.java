package de.adorsys.datasafe.business.api.types.action;

import de.adorsys.datasafe.business.api.types.DFSAccess;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ListRequest {

    @NonNull
    private final DFSAccess location;

    private final boolean decryptPath;
    private final ListRecursiveFlag recursiveFlag;
}
