package de.adorsys.docusafe2.business.impl.profile;

import dagger.Binds;
import dagger.Module;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;

@Module
public abstract class ProfileModule {

    @Binds
    abstract UserProfileService profileService(DefaultProfileService impl);
}
