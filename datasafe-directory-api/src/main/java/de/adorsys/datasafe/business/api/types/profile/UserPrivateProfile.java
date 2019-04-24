package de.adorsys.datasafe.business.api.types.profile;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@Builder
public class UserPrivateProfile {

    @NonNull
    private final URI keystore;

    @NonNull
    private final URI privateStorage;
}
