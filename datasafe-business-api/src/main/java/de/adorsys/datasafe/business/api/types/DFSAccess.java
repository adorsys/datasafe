package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.deployment.credentials.dto.DFSCredentials;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@Builder
public class DFSAccess {

    /**
     * Logical path - decrypted path value.
     */
    private final URI logicalPath;

    /**
     * Physical path - encrypted path value.
     */
    @NonNull
    private final URI physicalPath;

    /**
     * Credentials to access DFS system.
     */
    @NonNull
    private final DFSCredentials credentials;
}
