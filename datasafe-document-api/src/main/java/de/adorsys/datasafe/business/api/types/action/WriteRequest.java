package de.adorsys.datasafe.business.api.types.action;

import de.adorsys.datasafe.business.api.deployment.keystore.types.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class WriteRequest {

    @NonNull
    private final DFSAccess to;

    @NonNull
    private final PublicKeyIDWithPublicKey keyWithId;
}
