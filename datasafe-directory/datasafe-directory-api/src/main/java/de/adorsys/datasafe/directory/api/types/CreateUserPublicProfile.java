package de.adorsys.datasafe.directory.api.types;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

/**
 * Request to create user public profile part.
 */
@Value
@Builder(toBuilder = true)
public class CreateUserPublicProfile {

    /**
     * Public profile owner
     */
    @NonNull
    private final UserID id;

    /**
     * Location of users' public keys
     */
    @NonNull
    private final AbsoluteLocation<PublicResource> publicKeys;

    /**
     * Location of users' inbox where others can write documents (without list/read access)
     */
    @NonNull
    private final AbsoluteLocation<PublicResource> inbox;

    @SneakyThrows
    public UserPublicProfile buildPublicProfile() {
        return UserPublicProfile.builder()
            .inbox(inbox)
            .publicKeys(publicKeys)
            .build();
    }
}
