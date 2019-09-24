package de.adorsys.datasafe.simple.adapter.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.impl.directory.DefaultKeystoreCacheModule;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPublicKeyServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.DocumentKeyStoreOperationsImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.StorageKeyStoreOperationsImplRuntimeDelegatable;
import de.adorsys.datasafe.simple.adapter.impl.legacy.directory.LegacyDFSPrivateKeyServiceImpl;

/**
 * This module is responsible for credentials access - either user or dfs.
 */
@Module(includes = DefaultKeystoreCacheModule.class)
public abstract class LegacyCredentialsModule {

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
     * Private key service that expects there is only one path encryption key.
     */
    @Binds
    abstract PrivateKeyService privateKeyService(LegacyDFSPrivateKeyServiceImpl impl);
}
