package de.adorsys.docusafe2.business.api.profile;

import de.adorsys.docusafe2.business.api.profile.dto.ResolveRequest;
import de.adorsys.docusafe2.business.api.types.UserProfile;

public interface UserProfileService {

    /**
     * Resolves user's meta-information like folder mapping, etc.
     * @param forUser resolve request
     * @return fully-resolved user's profile
     */
    UserProfile user(ResolveRequest forUser);
}
