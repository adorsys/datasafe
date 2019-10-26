package de.adorsys.datasafe.simple.adapter.impl.profile;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRegistrationServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.storage.api.actions.StorageCheckService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;

import javax.inject.Inject;

/**
 * This service does not store user profile files, assuming profile paths are hardcoded relative to system root or
 * accessible using {@link DFSConfig}
 */
public class DFSRelativeProfileRegistrationServiceImpl extends ProfileRegistrationServiceImpl {

    @Inject
    public DFSRelativeProfileRegistrationServiceImpl(StorageKeyStoreOperations storageKeyStoreOper,
                                                     DocumentKeyStoreOperations keyStoreOper,
                                                     BucketAccessService access,
                                                     StorageCheckService checkService,
                                                     StorageWriteService writeService,
                                                     GsonSerde serde,
                                                     DFSConfig dfsConfig) {
        super(null, storageKeyStoreOper, keyStoreOper, access, checkService, writeService, serde, dfsConfig);
    }

    @Override
    public void registerPublic(CreateUserPublicProfile profile) {
        // NOP
    }

    @Override
    public void registerPrivate(CreateUserPrivateProfile profile) {
        // NOP
    }
}
