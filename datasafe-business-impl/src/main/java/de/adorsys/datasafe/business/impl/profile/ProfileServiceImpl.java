package de.adorsys.datasafe.business.impl.profile;

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
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/<>");
    }

    @Override
    public UserPrivateProfile privateProfile(UserIdAuth ofUser) {
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/<>");
    }
}
