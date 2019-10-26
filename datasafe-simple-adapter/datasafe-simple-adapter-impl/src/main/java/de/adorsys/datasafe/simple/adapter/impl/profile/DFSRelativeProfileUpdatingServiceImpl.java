package de.adorsys.datasafe.simple.adapter.impl.profile;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileUpdatingServiceImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

import javax.inject.Inject;

/**
 * This service does not store user profile files, assuming profile paths are hardcoded relative to system root or
 * accessible using {@link DFSConfig}
 */
public class DFSRelativeProfileUpdatingServiceImpl extends ProfileUpdatingServiceImpl {

    @Inject
    public DFSRelativeProfileUpdatingServiceImpl(PrivateKeyService privateKeyService,
                                                 StorageKeyStoreOperations storageKeyStoreOper,
                                                 DocumentKeyStoreOperations keyStoreOper) {
        super(null, privateKeyService, storageKeyStoreOper, keyStoreOper);
    }

    @Override
    public void updatePublicProfile(UserIDAuth forUser, UserPublicProfile profile) {
        // NOP
    }

    @Override
    public void updatePrivateProfile(UserIDAuth forUser, UserPrivateProfile profile) {
        // NOP
    }

    @Override
    public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
        super.updateReadKeyPassword(forUser, newPassword);
    }
}
