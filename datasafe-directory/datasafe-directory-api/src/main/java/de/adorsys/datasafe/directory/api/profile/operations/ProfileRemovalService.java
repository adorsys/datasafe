package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;

/**
 * Removes user from system.
 */
public interface ProfileRemovalService {

    /**
     * Removes both public and private profile from system associated with user
     * IMPORTANT! Ensure (in case of multi-tenant deployment) that all caches are cleared for removed user:
     * {@code KeyStoreCache}, {@code UserProfileCache} (if used) or use globally unique
     * username+password combination each time you create new user.
     * @param userID public and private profile owner
     */
    void deregister(UserIDAuth userID);
}
