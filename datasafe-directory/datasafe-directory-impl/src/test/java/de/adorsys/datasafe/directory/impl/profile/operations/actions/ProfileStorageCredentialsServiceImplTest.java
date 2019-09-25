package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

class ProfileStorageCredentialsServiceImplTest extends BaseMockitoTest {

    @Mock
    private PrivateKeyService privateKeyService;

    @Mock
    private StorageKeyStoreOperations storageKeyStoreOper;

    @InjectMocks
    private ProfileStorageCredentialsServiceImpl tested;

    @Mock
    private UserIDAuth user;

    @Mock
    private StorageIdentifier storageIdentifier;

    @Mock
    private StorageCredentials storageCredentials;

    @Test
    void registerStorageCredentials() {
        tested.registerStorageCredentials(user, storageIdentifier, storageCredentials);

        verify(privateKeyService).validateUserHasAccessOrThrow(user);
        verify(storageKeyStoreOper).addStorageCredentials(user, storageIdentifier, storageCredentials);
    }

    @Test
    void deregisterStorageCredentials() {
        tested.deregisterStorageCredentials(user, storageIdentifier);

        verify(privateKeyService).validateUserHasAccessOrThrow(user);
        verify(storageKeyStoreOper).removeStorageCredentials(user, storageIdentifier);
    }
}
