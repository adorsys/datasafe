package de.adorsys.docusafe2.business.api.types;

import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;
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
