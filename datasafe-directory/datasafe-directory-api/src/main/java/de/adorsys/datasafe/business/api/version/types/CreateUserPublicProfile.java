package de.adorsys.datasafe.business.api.version.types;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CreateUserPublicProfile {

    @NonNull
    private final UserID id;

    @NonNull
    private final AbsoluteResourceLocation<PublicResource> publicKeys;

    @NonNull
    private final AbsoluteResourceLocation<PublicResource> inbox;

    public UserPublicProfile removeAccess() {
        return UserPublicProfile.builder()
            // FIXME - remove access ?
            .inbox(inbox)
            .publicKeys(publicKeys)
            .build();
    }
}
