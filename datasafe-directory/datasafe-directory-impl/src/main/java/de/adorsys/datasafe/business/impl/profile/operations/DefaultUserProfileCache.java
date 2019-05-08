package de.adorsys.datasafe.business.impl.profile.operations;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.UserPublicProfile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class DefaultUserProfileCache implements UserProfileCache {

    private final Map<UserID, UserPublicProfile> publicProfile;
    private final Map<UserID, UserPrivateProfile> privateProfile;
}
