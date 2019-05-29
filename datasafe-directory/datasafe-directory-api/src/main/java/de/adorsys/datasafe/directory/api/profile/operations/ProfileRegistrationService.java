package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;

/**
 * Registers user in system.
 */
public interface ProfileRegistrationService {

    /**
     * Registers public part of user's profile - his INBOX and where are his public keys.
     * @param profile Public profile part
     */
    void registerPublic(CreateUserPublicProfile profile);

    /**
     * Registers private part of user profile - his keystore location, private folder location, etc.
     * Also creates his keystore if it does not exist at specified location and in that case publishes
     * users' public keys into location specified by users' public profile
     * @param profile Private profile part
     */
    void registerPrivate(CreateUserPrivateProfile profile);
}
