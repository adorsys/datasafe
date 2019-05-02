package de.adorsys.datasafe.business.impl.directory;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.business.impl.profile.resource.ResourceResolverImpl;

/**
 * This module is responsible for providing user profiles - his inbox, private storage, etc. locations.
 */
@Module
public abstract class DefaultProfileModule {

    @Binds
    abstract ProfileRetrievalService profileService(DFSBasedProfileStorageImpl impl);

    @Binds
    abstract ProfileRegistrationService creationService(DFSBasedProfileStorageImpl impl);

    @Binds
    abstract ResourceResolver resourceResolver(ResourceResolverImpl impl);
}
