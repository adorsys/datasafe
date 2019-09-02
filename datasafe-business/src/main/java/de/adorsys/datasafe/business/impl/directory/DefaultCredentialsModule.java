package de.adorsys.datasafe.business.impl.directory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.*;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyEntry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * This module is responsible for credentials access - either user or dfs.
 */
@Module
public abstract class DefaultCredentialsModule {

    /**
     * Default keystore and public key Guava-based cache. If one can't afford that some instances
     * may not see that storage access credentials were removed (for some time window they will be available)
     * or keystore password has changed, they can use any distributed cache available. But for most use cases
     * it is ok.
     */
    @Provides
    @Singleton
    static KeyStoreCache keyStoreCache(@Nullable OverridesRegistry registry) {

        Supplier<Cache<UserID, KeyStore>> cacheKeystore = () -> CacheBuilder.newBuilder()
                .initialCapacity(1000)
                // for this interval removed storage access key/changed keystore might not be seen
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();

        // These are actually static, so we can afford longer expiry time
        Supplier<Cache<UserID, List<PublicKeyEntry>>> cachePubKeys = () -> CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .build();

        return new DefaultKeyStoreCacheRuntimeDelegatable(
                registry,
                cachePubKeys.get().asMap(),
                cacheKeystore.get().asMap(),
                // it will generate new instance here
                cacheKeystore.get().asMap()
        );
    }

    /**
     * Default no-op service to get credentials to access filesystem.
     */
    @Binds
    abstract BucketAccessService bucketAccessService(BucketAccessServiceImplRuntimeDelegatable impl);

    /**
     * Default public key service that reads user public keys from the location specified by his profile inside DFS.
     */
    @Binds
    abstract PublicKeyService publicKeyService(DFSPublicKeyServiceImplRuntimeDelegatable impl);

    /**
     * Keystore(document) operations class that hides keystore access from other components.
     */
    @Binds
    abstract DocumentKeyStoreOperations docKeyStoreOperations(DocumentKeyStoreOperationsImplRuntimeDelegatable impl);

    /**
     * Keystore(storage credentials) operations class that hides keystore access from other components.
     */
    @Binds
    abstract StorageKeyStoreOperations storageKeyStoreOperations(StorageKeyStoreOperationsImplRuntimeDelegatable impl);

    /**
     * Default private key service that reads user private/secret keys from the location specified by his
     * profile inside DFS.
     */
    @Binds
    abstract PrivateKeyService privateKeyService(DFSPrivateKeyServiceImplRuntimeDelegatable impl);
}
