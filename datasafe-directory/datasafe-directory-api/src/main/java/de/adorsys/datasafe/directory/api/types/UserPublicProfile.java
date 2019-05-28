package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PublicResource;
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
