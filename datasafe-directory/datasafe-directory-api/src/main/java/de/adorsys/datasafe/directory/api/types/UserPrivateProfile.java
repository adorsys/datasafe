package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
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
