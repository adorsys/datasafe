package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CreateUserPrivateProfile {

    @NonNull
    private final UserIDAuth id;

    @NonNull
    private final AbsoluteLocation<PrivateResource> keystore;

    @NonNull
    private final AbsoluteLocation<PrivateResource> privateStorage;

    @NonNull
    private final AbsoluteLocation<PrivateResource> inboxWithWriteAccess;

    @NonNull
    private final AbsoluteLocation<PrivateResource> documentVersionStorage;

    private final AbsoluteLocation<PublicResource> publishPubKeysTo;

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
