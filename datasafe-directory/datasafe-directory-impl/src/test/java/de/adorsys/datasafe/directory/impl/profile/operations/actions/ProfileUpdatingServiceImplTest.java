package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
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

    @Test
    void updatePublicProfile() {
        tested.updatePublicProfile(user, publicProfile);

        verify(privateKeyService).validateUserHasAccessOrThrow(user);
        verify(storeService).registerPublic(user.getUserID(), publicProfile);
    }

    @Test
    void updatePrivateProfile() {
        tested.updatePrivateProfile(user, privateProfile);

        verify(privateKeyService).validateUserHasAccessOrThrow(user);
        verify(storeService).registerPrivate(user.getUserID(), privateProfile);
    }

    @Test
    void updateReadKeyPassword() {
        tested.updateReadKeyPassword(user, readKeyPassword);

        verify(privateKeyService, never()).validateUserHasAccessOrThrow(user);
        verify(keyStoreOper).updateReadKeyPassword(user, readKeyPassword);
    }
}
