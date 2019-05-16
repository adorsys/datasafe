package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class UserPrivateProfile {

    @NonNull
    private final AbsoluteLocation<PrivateResource> keystore;

    @NonNull
    private final AbsoluteLocation<PrivateResource> privateStorage;

    @NonNull
    private final AbsoluteLocation<PrivateResource> inboxWithFullAccess;

    @NonNull
    private final AbsoluteLocation<PrivateResource> documentVersionStorage;
}
