package de.adorsys.datasafe.directory.impl.profile.operations;

import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Default map-based cache that contains user profiles.
 */
@Getter
@RequiredArgsConstructor
public class DefaultUserProfileCache implements UserProfileCache {

    private final Map<UserID, UserPublicProfile> publicProfile;
    private final Map<UserID, UserPrivateProfile> privateProfile;
}
