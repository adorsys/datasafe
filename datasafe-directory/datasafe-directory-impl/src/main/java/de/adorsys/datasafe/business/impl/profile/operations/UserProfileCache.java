package de.adorsys.datasafe.business.impl.profile.operations;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.UserPublicProfile;

import java.util.Map;

public interface UserProfileCache {

    Map<UserID, UserPublicProfile> getPublicProfile();
    Map<UserID, UserPrivateProfile> getPrivateProfile();
}
