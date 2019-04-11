package de.adorsys.datasafe.business.impl.credentials;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.credentials.DFSCredentialsService;
import de.adorsys.datasafe.business.api.keystore.PrivateKeyService;
import de.adorsys.datasafe.business.api.keystore.PublicKeyService;

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

    @Binds
    abstract DFSCredentialsService dfsCredentialsService(DFSCredentialsServiceImpl impl);
}
