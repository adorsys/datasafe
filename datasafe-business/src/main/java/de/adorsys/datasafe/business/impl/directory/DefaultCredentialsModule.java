package de.adorsys.datasafe.business.impl.directory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.business.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPrivateKeyServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPublicKeyServiceImpl;

import java.util.concurrent.ConcurrentMap;

/**
 * This module is responsible for credentials access - either user or dfs.
 */
@Module
public abstract class DefaultCredentialsModule {

    @Provides
    static ConcurrentMap<UserID, KeyStoreAccess> keyStoreCache() {
        Cache<UserID, KeyStoreAccess> cache = CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .build();

        return cache.asMap();
    }

    @Binds
    abstract BucketAccessService bucketAccessService(BucketAccessServiceImpl impl);

    @Binds
    abstract PublicKeyService publicKeyService(DFSPublicKeyServiceImpl impl);

    @Binds
    abstract PrivateKeyService privateKeyService(DFSPrivateKeyServiceImpl impl);
}
