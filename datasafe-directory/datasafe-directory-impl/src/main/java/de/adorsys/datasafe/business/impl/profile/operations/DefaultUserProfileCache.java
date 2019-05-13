package de.adorsys.datasafe.business.impl.profile.operations;

import de.adorsys.datasafe.business.api.version.types.UserID;
import de.adorsys.datasafe.business.api.version.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.version.types.UserPublicProfile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class DefaultUserProfileCache implements UserProfileCache {

    private final Map<UserID, UserPublicProfile> publicProfile;
    private final Map<UserID, UserPrivateProfile> privateProfile;
}
