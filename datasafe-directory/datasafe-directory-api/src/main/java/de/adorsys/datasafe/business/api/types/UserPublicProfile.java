package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class UserPublicProfile {

    @NonNull
    private final AbsoluteLocation<PublicResource> publicKeys;

    @NonNull
    private final AbsoluteLocation<PublicResource> inbox;
}
