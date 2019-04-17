package de.adorsys.datasafe.business.api.deployment.profile;

import de.adorsys.datasafe.business.api.types.profile.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPublicProfile;

/**
 * This is a WIP part.
 */
public interface ProfileRegistrationService {

    void registerPublic(CreateUserPublicProfile profile);
    void registerPrivate(CreateUserPrivateProfile profile);
}
