package de.adorsys.datasafe.business.api.profile;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.profile.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.UserPublicProfile;

/**
 * This is a stub.
 */
public interface UserCreationService {

    void registerPublic(UserID ofUser, UserPublicProfile publicProfile);
    void registerPrivate(UserIDAuth ofUser, UserPrivateProfile privateProfile);
}
