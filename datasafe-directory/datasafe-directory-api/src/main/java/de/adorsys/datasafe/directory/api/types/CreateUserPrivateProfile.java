package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.global.Version;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
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
    private final AbsoluteLocation<PrivateResource> keystore;

    /**
     * Users' privatespace location (where his private files are stored)
     */
    private final AbsoluteLocation<PrivateResource> privateStorage;

    /**
     * Users' INBOX as privatespace location (INBOX folder with full control)
     */
    private final AbsoluteLocation<PrivateResource> inboxWithWriteAccess;

    /**
     * Where to store users' links to latest documents if software versioning is enabled.
     * Optional field used for software-versioning.
     */
    private final AbsoluteLocation<PrivateResource> documentVersionStorage;

    /**
     * Keystore that contains keys to access storage systems (i.e. s3 access key/secret key)
     * Optional field used for getting storage credentials.
     */
    private final AbsoluteLocation<PrivateResource> storageCredentialsKeystore;

    /**
     * If all files reside within some specific folder, one can simply remove it when deregistering user,
     * instead of removing files one-by-one - this is the list of such folders, or if we need to remove extra
     * associated resources with user.
     */
    private final List<AbsoluteLocation<PrivateResource>> associatedResources;

    /**
     * Where to publish users' public keys if it is necessary.
     */
    private final AbsoluteLocation<PublicResource> publishPubKeysTo;

    /**
     * Entity appVersion. Keeps version of datasafe which was used to create profile
     */
    @NonNull
    @Builder.Default
    private final Version appVersion = Version.current();

    public UserPrivateProfile buildPrivateProfile() {
        return UserPrivateProfile.builder()
            .keystore(keystore)
            .privateStorage(new HashMap<>(Collections.singletonMap(StorageIdentifier.DEFAULT, privateStorage)))
            .storageCredentialsKeystore(storageCredentialsKeystore)
            .inboxWithFullAccess(inboxWithWriteAccess)
            .documentVersionStorage(documentVersionStorage)
            .associatedResources(associatedResources)
            .publishPublicKeysTo(publishPubKeysTo)
            .appVersion(appVersion)
            .build();
    }
}
