package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.types.api.global.Version;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Users' public profile - typically should be seen only by owner.
 */
@Data
@Builder(toBuilder = true)
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

    /**
     * Entity appVersion. Keeps version (logical, not release) of datasafe which was used to create profile
     */
    @NonNull
    private final Version appVersion;
}
