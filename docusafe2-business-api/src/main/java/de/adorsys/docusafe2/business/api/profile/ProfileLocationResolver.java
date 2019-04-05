package de.adorsys.docusafe2.business.api.profile;

import de.adorsys.docusafe2.business.api.profile.dto.ProfileBucketPath;
import de.adorsys.docusafe2.business.api.types.UserId;

public interface ProfileLocationResolver {

    /**
     * Resolves user profile location that can be read and decrypted.
     * @param forUser user id
     * @return location of user profile
     */
    ProfileBucketPath resolveProfileLocation(UserId forUser);
}
