package de.adorsys.datasafe.business.impl.directory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileStorageCredentialsService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileUpdatingService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.DefaultUserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRegistrationServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRemovalServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileStorageCredentialsServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileUpdatingServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.resource.ResourceResolverImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * This module is responsible for providing user profiles - his inbox, private storage, etc. locations.
 */
@Module
public abstract class DefaultProfileModule {

    @Provides
    static KeyCreationConfig cmsEncryptionConfig(@Nullable EncryptionConfig config) {
        if (null == config) {
            return EncryptionConfig.builder().build().getKeys();
        }

        return config.getKeys();
    }

    /**
     * Default Guava-based user profile cache for public and private profile.
     */
    @Provides
    @Singleton
    static UserProfileCache userProfileCache(@Nullable OverridesRegistry registry) {
        Cache<UserID, UserPublicProfile> publicProfileCache = CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();

        Cache<UserID, UserPrivateProfile> privateProfileCache = CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();

        return new DefaultUserProfileCache(
                registry,
                publicProfileCache.asMap(),
                privateProfileCache.asMap()
        );
    }

    /**
     * Default profile reading service that simply reads json files with serialized public/private located on DFS.
     */
    @Binds
    abstract ProfileRetrievalService profileRetrievalService(ProfileRetrievalServiceImpl impl);

    /**
     * Default profile creation service that simply creates keystore, public keys, user profile json files on DFS.
     */
    @Binds
    abstract ProfileRegistrationService creationService(ProfileRegistrationServiceImpl impl);

    /**
     * Default profile updating service.
     */
    @Binds
    abstract ProfileUpdatingService updatingService(ProfileUpdatingServiceImpl impl);

    /**
     * Default profile removal service.
     */
    @Binds
    abstract ProfileRemovalService removalService(ProfileRemovalServiceImpl impl);

    /**
     * Storage credentials access.
     */
    @Binds
    abstract ProfileStorageCredentialsService profileStorageCredentialsService(ProfileStorageCredentialsServiceImpl impl);

    /**
     * Resource resolver that simply prepends relevant path segment from profile based on location type.
     */
    @Binds
    abstract ResourceResolver resourceResolver(ResourceResolverImpl impl);

    /**
     * Aggregate service for profile operations.
     */
    @Binds
    abstract ProfileOperations profileService(DFSBasedProfileStorageImpl impl);
}
