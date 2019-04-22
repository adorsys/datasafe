package de.adorsys.datasafe.business.impl.profile;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;

/**
 * This module is responsible for providing user profiles - his inbox, private storage, etc. locations.
 */
@Module
public abstract class DefaultProfileModule {

    @Binds
    abstract ProfileRetrievalService profileService(DFSBasedProfileStorageImpl impl);

    @Binds
    abstract ProfileRegistrationService creationService(DFSBasedProfileStorageImpl impl);
}
