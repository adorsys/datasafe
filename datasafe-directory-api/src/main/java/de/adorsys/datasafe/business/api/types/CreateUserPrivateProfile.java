package de.adorsys.datasafe.business.api.types;

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

    @NonNull
    private final PrivateResource inboxWithWriteAccess;

    public UserPrivateProfile removeAccess() {
        return UserPrivateProfile.builder()
            // FIXME - remove access ?
            .keystore(keystore)
            .privateStorage(privateStorage)
            .inboxWithWriteAccess(inboxWithWriteAccess)
            .build();
    }
}
