package de.adorsys.docusafe2.business.api.types;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.credentials.dto.DFSCredentials;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DFSAccess {

    /**
     * Logical path - decrypted path value.
     */
    @NonNull
    private final BucketPath path;

    /**
     * Physical path - encrypted path value.
     */
    @NonNull
    private final BucketPath physicalPath;

    /**
     * Credentials to access DFS system.
     */
    @NonNull
    private final DFSCredentials credentials;
}
