package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

/**
 * Users' private profile - typically should be seen only by owner.
 */
@Data
@Builder(toBuilder = true)
public class UserPrivateProfile {

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
    private final AbsoluteLocation<PrivateResource> inboxWithFullAccess;

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
     * If all files reside within some specific folder, one can simply remove it when deregistering user,
     * instead of removing files one-by-one - this is the list of such folders, or if we need to remove extra
     * associated resources with user.
     */
    @NonNull
    private final List<AbsoluteLocation<PrivateResource>> associatedResources;
}
