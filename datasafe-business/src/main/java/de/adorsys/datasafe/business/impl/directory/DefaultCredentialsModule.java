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
import de.adorsys.datasafe.business.api.types.cobertura.CoberturaIgnore;
import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPrivateKeyServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DFSPublicKeyServiceImpl;
import de.adorsys.datasafe.business.impl.profile.keys.DefaultKeyStoreCache;
import de.adorsys.datasafe.business.impl.profile.keys.KeyStoreCache;

import javax.inject.Singleton;
import java.security.KeyStore;
import java.util.List;
import java.util.function.Supplier;

/**
 * This module is responsible for credentials access - either user or dfs.
 */
@Module
public abstract class DefaultCredentialsModule {

    @CoberturaIgnore
    @Provides
    @Singleton
    static KeyStoreCache keyStoreCache() {
        Supplier<Cache<UserID, KeyStore>> cacheKeystore = () -> CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .build();

        Supplier<Cache<UserID, List<PublicKeyIDWithPublicKey>>> cachePubKeys = () -> CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .build();

        return new DefaultKeyStoreCache(cachePubKeys.get().asMap(), cacheKeystore.get().asMap());
    }

    @CoberturaIgnore
    @Binds
    abstract BucketAccessService bucketAccessService(BucketAccessServiceImpl impl);

    @CoberturaIgnore
    @Binds
    abstract PublicKeyService publicKeyService(DFSPublicKeyServiceImpl impl);

    @CoberturaIgnore
    @Binds
    abstract PrivateKeyService privateKeyService(DFSPrivateKeyServiceImpl impl);
}
