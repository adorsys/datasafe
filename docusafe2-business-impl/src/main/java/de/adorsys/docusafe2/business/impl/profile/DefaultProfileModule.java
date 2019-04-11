package de.adorsys.docusafe2.business.impl.profile;

import dagger.Binds;
import dagger.Module;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;

/**
 * This module is responsible for providing user profiles - his inbox, private storage, etc. locations.
 */
@Module
public abstract class DefaultProfileModule {

    @Binds
    abstract UserProfileService profileService(ProfileServiceImpl impl);
}
