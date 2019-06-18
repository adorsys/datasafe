package de.adorsys.datasafe.directory.impl.profile.config;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;

public interface UserProfileLocation {
    AbsoluteLocation<PrivateResource> locatePrivateProfile(UserID ofUser);
    AbsoluteLocation<PublicResource> locatePublicProfile(UserID ofUser);

}
