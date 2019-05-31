package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

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
