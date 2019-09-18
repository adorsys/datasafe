package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.types.api.global.Version;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Users' private profile - typically should be seen only by owner.
 */
@Data
@Builder(toBuilder = true)
public class UserPrivateProfile{

    /**
     * Users' keystore location
     */
    @NonNull
    private final AbsoluteLocation<PrivateResource> keystore;

    /**
     * Users' privatespace location (where his private files are stored)
     */
    @NonNull
    private final Map<StorageIdentifier, AbsoluteLocation<PrivateResource>> privateStorage;

    /**
     * Users' INBOX as privatespace location (INBOX folder with full control)
     */
    @NonNull
    private final AbsoluteLocation<PrivateResource> inboxWithFullAccess;

    /**
     * Where one should publish public keys.
     */
    @NonNull
    private final AbsoluteLocation<PublicResource> publishPublicKeysTo;

    /**
     * If all files reside within some specific folder, one can simply remove it when deregistering user,
     * instead of removing files one-by-one - this is the list of such folders, or if we need to remove extra
     * associated resources with user.
     */
    @NonNull
    private final List<AbsoluteLocation<PrivateResource>> associatedResources;

    /**
     * Where to store users' links to latest documents if software versioning is enabled.
     * Optional field used for software-versioning.
     */
    private final AbsoluteLocation<PrivateResource> documentVersionStorage;

    /**
     * Keystore that contains keys to access storage systems (i.e. s3 access key/secret key).
     * Optional field used for getting storage credentials using default flow.
     */
    private final AbsoluteLocation<PrivateResource> storageCredentialsKeystore;

    /**
     * Entity appVersion. Keeps appVersion of datasafe which was used to create profile
     */
    @NonNull
    private final Version appVersion;
}
