package de.adorsys.datasafe.business.impl.directory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPrivateKeyServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPublicKeyServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.keys.DefaultKeyStoreCache;
import de.adorsys.datasafe.directory.impl.profile.keys.KeyStoreCache;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;

import javax.inject.Singleton;
import java.security.KeyStore;
import java.util.List;
import java.util.function.Supplier;

/**
 * This module is responsible for credentials access - either user or dfs.
 */
@Module
public abstract class DefaultCredentialsModule {

    /**
     * Default keystore and public key Guava-based cache.
     */
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

    /**
     * Default no-op service to get credentials to access filesystem.
     */
    @Binds
    abstract BucketAccessService bucketAccessService(BucketAccessServiceImpl impl);

    /**
     * Default public key service that reads user public keys from the location specified by his profile inside DFS.
     */
    @Binds
    abstract PublicKeyService publicKeyService(DFSPublicKeyServiceImpl impl);

    /**
     * Default private key service that reads user private/secret keys from the location specified by his
     * profile inside DFS.
     */
    @Binds
    abstract PrivateKeyService privateKeyService(DFSPrivateKeyServiceImpl impl);
}
