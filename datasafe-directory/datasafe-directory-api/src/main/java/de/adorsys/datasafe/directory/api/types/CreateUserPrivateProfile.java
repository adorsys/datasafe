package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Request to create private user profile part.
 */
@Value
@Builder(toBuilder = true)
public class CreateUserPrivateProfile {

    /**
     * Users' credentials used to access underlying systems.
     */
    @NonNull
    private final UserIDAuth id;

    /**
     * Users' keystore location
     */
    @NonNull
    private final AbsoluteLocation<PrivateResource> keystore;

    /**
     * Users' privatespace location (where his private files are stored)
     */
    @NonNull
    private final AbsoluteLocation<PrivateResource> privateStorage;

    /**
     * Users' INBOX as privatespace location (INBOX folder with full control)
     */
    @NonNull
    private final AbsoluteLocation<PrivateResource> inboxWithWriteAccess;

    /**
     * Where to store users' links to latest documents if software versioning is enabled.
     */
    private final AbsoluteLocation<PrivateResource> documentVersionStorage;

    /**
     * If all files reside within some specific folder, one can simply remove it when deregistering user,
     * instead of removing files one-by-one - this is the list of such folders.
     */
    @NonNull
    private final List<AbsoluteLocation<PrivateResource>> associatedResources;

    /**
     * Where to publish users' public keys if it is necessary.
     */
    private final AbsoluteLocation<PublicResource> publishPubKeysTo;

    public UserPrivateProfile removeAccess() {
        return UserPrivateProfile.builder()
            // FIXME - remove access ?
            .keystore(keystore)
            .privateStorage(privateStorage)
            .inboxWithFullAccess(inboxWithWriteAccess)
            .documentVersionStorage(documentVersionStorage)
            .associatedResources(associatedResources)
            .build();
    }
}
