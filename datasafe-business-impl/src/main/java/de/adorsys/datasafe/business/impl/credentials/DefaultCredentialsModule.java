package de.adorsys.datasafe.business.impl.credentials;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.api.directory.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.directory.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.storage.dfs.BucketAccessService;

/**
 * This module is responsible for credentials access - either user or dfs.
 */
@Module
public abstract class DefaultCredentialsModule {

    @Binds
    abstract BucketAccessService bucketAccessService(BucketAccessServiceImpl impl);

    @Binds
    abstract PublicKeyService publicKeyService(DFSPublicKeyServiceImpl impl);

    @Binds
    abstract PrivateKeyService privateKeyService(DFSPrivateKeyServiceImpl impl);

    @Provides
    abstract SystemCredentialsServiceImpl dfsCredentialsService(SystemCredentialsServiceImpl impl);
}
