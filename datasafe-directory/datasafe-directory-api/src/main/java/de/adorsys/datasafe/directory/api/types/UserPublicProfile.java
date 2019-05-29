package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Users' public profile - typically should be seen only by owner.
 */
@Value
@Builder
public class UserPublicProfile {

    /**
     * Public profile owner
     */
    @NonNull
    private final AbsoluteLocation<PublicResource> publicKeys;

    /**
     * Location of users' inbox where others can write documents (without list/read access)
     */
    @NonNull
    private final AbsoluteLocation<PublicResource> inbox;
}
