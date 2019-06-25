package de.adorsys.datasafe.directory.impl.profile.config;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;

/**
 * Bootstrap for user profile-file locations.
 */
public interface UserProfileLocation {

    /**
     * Bootstraps location of file with user private profile
     * @param ofUser user for whom to get profile
     * @return Location of users' private profile file
     */
    AbsoluteLocation<PrivateResource> locatePrivateProfile(UserID ofUser);

    /**
     * Bootstraps location of file with user public profile
     * @param ofUser user for whom to get profile
     * @return Location of users' public profile file
     */
    AbsoluteLocation<PublicResource> locatePublicProfile(UserID ofUser);
}
