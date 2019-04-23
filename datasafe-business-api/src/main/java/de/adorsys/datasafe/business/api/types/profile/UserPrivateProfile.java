package de.adorsys.datasafe.business.api.types.profile;

import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class UserPrivateProfile {

    @NonNull
    private final PrivateResource keystore;

    @NonNull
    private final PrivateResource privateStorage;

    @NonNull
    private final PrivateResource inboxWithWriteAccess;
}
