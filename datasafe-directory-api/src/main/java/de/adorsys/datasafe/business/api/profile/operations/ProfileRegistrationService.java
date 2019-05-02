package de.adorsys.datasafe.business.api.profile.operations;

import de.adorsys.datasafe.business.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.CreateUserPublicProfile;

/**
 * This is a WIP part.
 */
public interface ProfileRegistrationService {

    void registerPublic(CreateUserPublicProfile profile);
    void registerPrivate(CreateUserPrivateProfile profile);
}
