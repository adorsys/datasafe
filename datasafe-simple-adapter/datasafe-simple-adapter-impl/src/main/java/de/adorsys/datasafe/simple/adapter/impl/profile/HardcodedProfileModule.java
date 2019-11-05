package de.adorsys.datasafe.simple.adapter.impl.profile;

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
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileStorageCredentialsServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.resource.ResourceResolverImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;

import javax.annotation.Nullable;

@Module
public abstract class HardcodedProfileModule {

    @Provides
    static KeyCreationConfig cmsEncryptionConfig(@Nullable EncryptionConfig config) {
        if (null == config) {
            return EncryptionConfig.builder().build().getKeys();
        }

        return config.getKeys();
    }

    /**
     * Default profile reading service that simply reads json files with serialized public/private located on DFS.
     */
    @Binds
    abstract ProfileRetrievalService profileRetrievalService(DFSRelativeProfileRetrievalServiceImpl impl);

    /**
     * Default profile creation service that simply creates keystore, public keys, user profile json files on DFS.
     */
    @Binds
    abstract ProfileRegistrationService creationService(DFSRelativeProfileRegistrationServiceImpl impl);

    /**
     * Default profile updating service.
     */
    @Binds
    abstract ProfileUpdatingService updatingService(DFSRelativeProfileUpdatingServiceImpl impl);

    /**
     * Default profile removal service.
     */
    @Binds
    abstract ProfileRemovalService removalService(DFSRelativeProfileRemovalServiceImpl impl);

    /**
     * Storage credentials access.
     */
    @Binds
    abstract ProfileStorageCredentialsService profileStorageCredentialsService(ProfileStorageCredentialsServiceImplRuntimeDelegatable impl);

    /**
     * Resource resolver that simply prepends relevant path segment from profile based on location type.
     */
    @Binds
    abstract ResourceResolver resourceResolver(ResourceResolverImplRuntimeDelegatable impl);

    /**
     * Aggregate service for profile operations.
     */
    @Binds
    abstract ProfileOperations profileService(DFSBasedProfileStorageImplRuntimeDelegatable impl);
}
