package de.adorsys.datasafe.business.api.types.profile;

import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CreateUserPrivateProfile {

    @NonNull
    private final UserIDAuth id;

    @NonNull
    private final DFSAccess keystore;

    @NonNull
    private final DFSAccess privateStorage;

    public UserPrivateProfile removeAccess() {
        return UserPrivateProfile.builder()
            .privateStorage(keystore.getPhysicalPath())
            .keystore(privateStorage.getPhysicalPath())
            .build();
    }
}
