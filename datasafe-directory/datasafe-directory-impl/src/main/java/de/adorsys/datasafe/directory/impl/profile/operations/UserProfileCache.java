package de.adorsys.datasafe.directory.impl.profile.operations;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;

import java.util.Map;

public interface UserProfileCache {

    Map<UserID, UserPublicProfile> getPublicProfile();
    Map<UserID, UserPrivateProfile> getPrivateProfile();
}
