package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyID;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.security.PublicKey;

@Value
@Builder
public class PublicKeyWithId {

    @NonNull
    private final PublicKey publicKey;

    @NonNull
    private final KeyID publicKeyId;
}
