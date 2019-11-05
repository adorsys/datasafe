package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileUpdatingService;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
@RuntimeDelegate
public class ProfileUpdatingServiceImpl implements ProfileUpdatingService {

    private final ProfileStoreService storeService;
    private final PrivateKeyService privateKeyService;
    private final StorageKeyStoreOperations storageKeyStoreOper;
    private final DocumentKeyStoreOperations keyStoreOper;

    @Inject
    public ProfileUpdatingServiceImpl(ProfileStoreService storeService, PrivateKeyService privateKeyService,
                                      StorageKeyStoreOperations storageKeyStoreOper,
                                      DocumentKeyStoreOperations keyStoreOper) {
        this.storeService = storeService;
        this.privateKeyService = privateKeyService;
        this.storageKeyStoreOper = storageKeyStoreOper;
        this.keyStoreOper = keyStoreOper;
    }

    @Override
    @SneakyThrows
    public void updatePublicProfile(UserIDAuth forUser, UserPublicProfile profile) {
        validateKeystoreAccess(forUser);
        log.debug("Update public profile {}", profile);
        storeService.registerPublic(forUser.getUserID(), profile);
    }

    @Override
    public void updatePrivateProfile(UserIDAuth forUser, UserPrivateProfile profile) {
        validateKeystoreAccess(forUser);
        log.debug("Update private profile {}", profile);
        storeService.registerPrivate(forUser.getUserID(), profile);
    }

    @Override
    public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
        log.debug("Update read key for profile {}", forUser.getUserID());
        // access check is implicit
        keyStoreOper.updateReadKeyPassword(forUser, newPassword);
        storageKeyStoreOper.updateReadKeyPassword(forUser, newPassword);
    }

    @SneakyThrows
    private void validateKeystoreAccess(UserIDAuth user) {
        privateKeyService.validateUserHasAccessOrThrow(user);
    }
}
