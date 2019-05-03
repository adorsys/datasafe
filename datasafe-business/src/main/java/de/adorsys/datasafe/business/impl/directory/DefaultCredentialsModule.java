package de.adorsys.datasafe.business.impl.directory;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPrivateKeyServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPublicKeyServiceImpl;

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
}
