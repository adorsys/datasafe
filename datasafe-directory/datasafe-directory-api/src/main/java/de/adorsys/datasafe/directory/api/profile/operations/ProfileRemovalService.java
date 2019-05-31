package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

/**
 * Removes user from system.
 */
public interface ProfileRemovalService {

    /**
     * Removes both public and private profile from system associated with user
     * @param userID public and private profile owner
     */
    void deregister(UserIDAuth userID);
}
