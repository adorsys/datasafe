package de.adorsys.datasafe.directory.impl.profile.operations;

import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.Getter;

import javax.inject.Inject;
import java.util.Map;

/**
 * Default map-based cache that contains user profiles.
 */
@Getter
@RuntimeDelegate
public class DefaultUserProfileCache implements UserProfileCache {

    private final Map<UserID, UserPublicProfile> publicProfile;
    private final Map<UserID, UserPrivateProfile> privateProfile;

    @Inject
    public DefaultUserProfileCache(Map<UserID, UserPublicProfile> publicProfile,
                                   Map<UserID, UserPrivateProfile> privateProfile) {
        this.publicProfile = publicProfile;
        this.privateProfile = privateProfile;
    }
}
