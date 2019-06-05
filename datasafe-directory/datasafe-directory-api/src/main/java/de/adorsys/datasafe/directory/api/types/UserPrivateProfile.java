package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

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
     */
    private final AbsoluteLocation<PrivateResource> documentVersionStorage;
}
