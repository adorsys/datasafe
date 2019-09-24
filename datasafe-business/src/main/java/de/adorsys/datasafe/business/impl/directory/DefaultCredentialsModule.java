package de.adorsys.datasafe.business.impl.directory;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPrivateKeyServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPublicKeyServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.DocumentKeyStoreOperationsImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.keys.StorageKeyStoreOperationsImplRuntimeDelegatable;

/**
 * This module is responsible for credentials access - either user or dfs.
 */
@Module(includes = DefaultKeystoreCacheModule.class)
public abstract class DefaultCredentialsModule {

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
