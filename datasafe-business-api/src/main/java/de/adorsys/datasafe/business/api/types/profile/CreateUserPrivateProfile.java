package de.adorsys.datasafe.business.api.types.profile;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CreateUserPrivateProfile {

    @NonNull
    private final UserIDAuth id;

    @NonNull
    private final PrivateResource keystore;

    @NonNull
    private final PrivateResource privateStorage;

    public UserPrivateProfile removeAccess() {
        return UserPrivateProfile.builder()
            // FIXME - remove access ?
            .keystore(keystore)
            .privateStorage(privateStorage)
            .build();
    }
}
