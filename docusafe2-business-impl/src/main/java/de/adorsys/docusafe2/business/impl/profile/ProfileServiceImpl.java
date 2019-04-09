package de.adorsys.docusafe2.business.impl.profile;

import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.UserId;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;
import de.adorsys.docusafe2.business.api.types.profile.UserPrivateProfile;
import de.adorsys.docusafe2.business.api.types.profile.UserPublicProfile;

import javax.inject.Inject;

public class ProfileServiceImpl implements UserProfileService {

    @Inject
    public ProfileServiceImpl() {
    }

    @Override
    public UserPublicProfile publicProfile(UserId ofUser) {
        return null;
    }

    @Override
    public UserPrivateProfile privateProfile(UserIdAuth ofUser) {
        return null;
    }
}
