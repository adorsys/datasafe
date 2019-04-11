package de.adorsys.datasafe.business.impl.profile;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.profile.UserProfileService;

/**
 * This module is responsible for providing user profiles - his inbox, private storage, etc. locations.
 */
@Module
public abstract class DefaultProfileModule {

    @Binds
    abstract UserProfileService profileService(ProfileServiceImpl impl);
}
