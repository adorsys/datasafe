package de.adorsys.datasafe.business.api.profile.operations;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.UserPublicProfile;

public interface ProfileRetrievalService {

    /**
     * Resolves user's public meta-information like folder mapping, etc.
     * @param ofUser resolve request
     * @return resolved user's profile
     */
    UserPublicProfile publicProfile(UserID ofUser);

    /**
     * Resolves user's private meta-information like folder mapping, etc.
     * @param ofUser resolve request
     * @return resolved user's profile
     */
    UserPrivateProfile privateProfile(UserIDAuth ofUser);

    boolean userExists(UserID ofUser);
}
