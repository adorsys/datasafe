package de.adorsys.datasafe.business.api.types.profile;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@Builder
public class UserPublicProfile {

    @NonNull
    private final URI publicKeys;

    @NonNull
    private final URI inbox;
}
