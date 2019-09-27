package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileStorageCredentialsService;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;

import javax.inject.Inject;
import java.util.Set;

@RuntimeDelegate
public class ProfileStorageCredentialsServiceImpl implements ProfileStorageCredentialsService {

    private final StorageKeyStoreOperations keyStoreOper;
    private final PrivateKeyService privateKeyService;

    @Inject
    public ProfileStorageCredentialsServiceImpl(StorageKeyStoreOperations keyStoreOper,
                                                PrivateKeyService privateKeyService) {
        this.keyStoreOper = keyStoreOper;
        this.privateKeyService = privateKeyService;
    }

    @Override
    public Set<StorageIdentifier> listRegisteredStorageCredentials(UserIDAuth user) {
        validateKeystoreAccess(user);
        return keyStoreOper.readAliases(user);
    }

    @Override
    public void registerStorageCredentials(
            UserIDAuth user, StorageIdentifier storageId, StorageCredentials credentials) {
        validateKeystoreAccess(user);
        keyStoreOper.addStorageCredentials(user, storageId, credentials);
    }

    @Override
    public void deregisterStorageCredentials(UserIDAuth user, StorageIdentifier storageId) {
        validateKeystoreAccess(user);
        keyStoreOper.removeStorageCredentials(user, storageId);
    }

    private void validateKeystoreAccess(UserIDAuth user) {
        // avoid only unauthorized access
        privateKeyService.validateUserHasAccessOrThrow(user); // for access check
    }
}
