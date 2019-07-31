package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileUpdatingService;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;

import javax.inject.Inject;

@RuntimeDelegate
public class ProfileUpdatingServiceImpl implements ProfileUpdatingService {

    private final StorageKeyStoreOperations storageKeyStoreOper;
    private final DocumentKeyStoreOperations keyStoreOper;

    @Inject
    public ProfileUpdatingServiceImpl(StorageKeyStoreOperations storageKeyStoreOper,
                                      DocumentKeyStoreOperations keyStoreOper) {
        this.storageKeyStoreOper = storageKeyStoreOper;
        this.keyStoreOper = keyStoreOper;
    }

    @Override
    public void updatePublicProfile(UserIDAuth forUser, UserPublicProfile profile) {

    }

    @Override
    public void updatePrivateProfile(UserIDAuth forUser, UserPrivateProfile profile) {

    }

    @Override
    public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
        keyStoreOper.updateReadKeyPassword(forUser, newPassword);
        storageKeyStoreOper.updateReadKeyPassword(forUser, newPassword);
    }

    @Override
    public void registerStorageCredentials(
            UserIDAuth user, StorageIdentifier storageId, StorageCredentials credentials) {
        storageKeyStoreOper.addStorageCredentials(user, storageId, credentials);
    }

    @Override
    public void deregisterStorageCredentials(UserIDAuth user, StorageIdentifier storageId) {

    }
}
