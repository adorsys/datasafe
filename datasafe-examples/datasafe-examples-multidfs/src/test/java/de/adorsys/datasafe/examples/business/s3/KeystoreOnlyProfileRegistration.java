package de.adorsys.datasafe.examples.business.s3;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.KeyStoreOperations;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRegistrationServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.storage.api.actions.StorageCheckService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;

/**
 * When new DFS is registered, only keystore for private files stored in it is created. All other stuff
 * like user profile, his public keys, etc. is manages solely by Directory Datasafe and so is not created.
 */
public class KeystoreOnlyProfileRegistration extends ProfileRegistrationServiceImpl {

    public KeystoreOnlyProfileRegistration(KeyStoreOperations keyStoreOper, BucketAccessService access,
                                           StorageCheckService checkService, StorageWriteService writeService, GsonSerde serde, DFSConfig dfsConfig) {
        super(keyStoreOper, access, checkService, writeService, serde, dfsConfig);
    }
}
