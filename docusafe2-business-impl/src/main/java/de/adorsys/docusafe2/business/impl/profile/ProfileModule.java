package de.adorsys.docusafe2.business.impl.profile;

import dagger.Module;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;

@Module
public class ProfileModule {

    UserProfileService profileService() {
        return new DefaultProfileService();
    }
}
