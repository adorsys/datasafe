package de.adorsys.datasafe.business.api.types.action;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InboxWriteRequest {

    @NonNull
    private final UserID from;

    @NonNull
    private final UserID to;

    @NonNull
    private final PublicKeyIDWithPublicKey keyWithId;
}
