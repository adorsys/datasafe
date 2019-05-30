package de.adorsys.datasafe.business.impl.directory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.DefaultUserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.resource.ResourceResolverImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserID;

import javax.inject.Singleton;

/**
 * This module is responsible for providing user profiles - his inbox, private storage, etc. locations.
 */
@Module
public abstract class DefaultProfileModule {

    /**
     * Default Guava-based user profile cache for public and private profile.
     */
    @Provides
    @Singleton
    static UserProfileCache userProfileCache() {
        Cache<UserID, UserPublicProfile> publicProfileCache = CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .build();
        Cache<UserID, UserPrivateProfile> privateProfileCache = CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .build();

        return new DefaultUserProfileCache(publicProfileCache.asMap(), privateProfileCache.asMap());
    }

    /**
     * Default profile reading service that simply reads json files with serialized public/private located on DFS.
     */
    @Binds
    abstract ProfileRetrievalService profileService(DFSBasedProfileStorageImpl impl);

    /**
     * Default profile creation service that simply creates keystore, public keys, user profile json files on DFS.
     */
    @Binds
    abstract ProfileRegistrationService creationService(DFSBasedProfileStorageImpl impl);

    /**
     * Resource resolver that simply prepends relevant path segment from profile based on location type.
     */
    @Binds
    abstract ResourceResolver resourceResolver(ResourceResolverImpl impl);
}
