package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

/**
 * User public and private profile data accessor. Describes how users' virtual filesystem is laid out -
 * where he has INBOX, where he has privatespace, where his keystore is located, etc.
 */
public interface ProfileRetrievalService {

    /**
     * Resolves user's public meta-information like INBOX, public keys, etc. folder mapping.
     * @param ofUser resolve request
     * @return resolved user's profile
     */
    UserPublicProfile publicProfile(UserID ofUser);

    /**
     * Resolves user's private meta-information like privatespace, keystore, etc. folder mapping.
     * @param ofUser resolve request
     * @return resolved user's profile
     */
    UserPrivateProfile privateProfile(UserIDAuth ofUser);

    boolean userExists(UserID ofUser);
}
