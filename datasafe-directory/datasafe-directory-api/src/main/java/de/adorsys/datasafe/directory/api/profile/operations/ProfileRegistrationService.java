package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;

/**
 * This is a WIP part.
 */
public interface ProfileRegistrationService {

    void registerPublic(CreateUserPublicProfile profile);
    void registerPrivate(CreateUserPrivateProfile profile);
}
