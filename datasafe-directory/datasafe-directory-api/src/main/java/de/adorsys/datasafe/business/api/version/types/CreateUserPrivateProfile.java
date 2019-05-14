package de.adorsys.datasafe.business.api.version.types;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
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
    private final AbsoluteResourceLocation<PrivateResource> keystore;

    @NonNull
    private final AbsoluteResourceLocation<PrivateResource> privateStorage;

    @NonNull
    private final AbsoluteResourceLocation<PrivateResource> inboxWithWriteAccess;

    @NonNull
    private final AbsoluteResourceLocation<PrivateResource> documentVersionStorage;

    public UserPrivateProfile removeAccess() {
        return UserPrivateProfile.builder()
            // FIXME - remove access ?
            .keystore(keystore)
            .privateStorage(privateStorage)
            .inboxWithFullAccess(inboxWithWriteAccess)
            .documentVersionStorage(documentVersionStorage)
            .build();
    }
}
