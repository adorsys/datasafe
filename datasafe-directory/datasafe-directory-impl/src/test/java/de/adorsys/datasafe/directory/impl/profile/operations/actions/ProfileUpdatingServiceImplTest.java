package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ProfileUpdatingServiceImplTest extends BaseMockitoTest {

    @Mock
    private ProfileStoreService storeService;

    @Mock
    private PrivateKeyService privateKeyService;

    @Mock
    private StorageKeyStoreOperations storageKeyStoreOper;

    @Mock
    private DocumentKeyStoreOperations keyStoreOper;

    @InjectMocks
    private ProfileUpdatingServiceImpl tested;

    @Mock
    private UserIDAuth user;

    @Mock
    private UserPublicProfile publicProfile;

    @Mock
    private UserPrivateProfile privateProfile;

    @Mock
    private ReadKeyPassword readKeyPassword;

    @Mock
    private StorageIdentifier storageIdentifier;

    @Mock
    private StorageCredentials storageCredentials;

    @Test
    void updatePublicProfile() {
        tested.updatePublicProfile(user, publicProfile);

        verify(privateKeyService).documentEncryptionSecretKey(user);
        verify(storeService).registerPublic(user.getUserID(), publicProfile);
    }

    @Test
    void updatePrivateProfile() {
        tested.updatePrivateProfile(user, privateProfile);

        verify(privateKeyService).documentEncryptionSecretKey(user);
        verify(storeService).registerPrivate(user.getUserID(), privateProfile);
    }

    @Test
    void updateReadKeyPassword() {
        tested.updateReadKeyPassword(user, readKeyPassword);

        verify(privateKeyService, never()).documentEncryptionSecretKey(user);
        verify(keyStoreOper).updateReadKeyPassword(user, readKeyPassword);
    }

    @Test
    void registerStorageCredentials() {
        tested.registerStorageCredentials(user, storageIdentifier, storageCredentials);

        verify(privateKeyService).documentEncryptionSecretKey(user);
        verify(storageKeyStoreOper).addStorageCredentials(user, storageIdentifier, storageCredentials);
    }

    @Test
    void deregisterStorageCredentials() {
        tested.deregisterStorageCredentials(user, storageIdentifier);

        verify(privateKeyService).documentEncryptionSecretKey(user);
        verify(storageKeyStoreOper).removeStorageCredentials(user, storageIdentifier);
    }
}
