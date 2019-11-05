package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StorageKeyStoreOperationsImplTest extends BaseMockitoTest {

    private static final String STORAGE_ID = "id";
    private static final ReadKeyPassword SECRET = ReadKeyPasswordTestFactory.getForString("secret");
    private static final AbsoluteLocation<PrivateResource> STORAGE_KEYSTORE =
            BasePrivateResource.forAbsolutePrivate("file://path");

    @Mock
    private GsonSerde gson;

    @Mock
    private KeyStoreService keyStoreService;

    @Mock
    private GenericKeystoreOperations genericOper;

    @Mock
    private ProfileRetrievalService profile;

    @Mock
    private BucketAccessService access;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KeyStoreCache keystoreCache;

    @Mock
    private UserPrivateProfile privateProfile;

    private UserIDAuth user = new UserIDAuth("john", SECRET);

    private StorageIdentifier storageId = new StorageIdentifier(STORAGE_ID);

    @InjectMocks
    private StorageKeyStoreOperationsImpl tested;

    @BeforeEach
    void init() {
        when(profile.privateProfile(user)).thenReturn(privateProfile);
        when(privateProfile.getStorageCredentialsKeystore()).thenReturn(STORAGE_KEYSTORE);
        when(access.withSystemAccess(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void removeStorageCredentials() {
        tested.removeStorageCredentials(user, storageId);

        verify(keyStoreService).removeKey(any(), eq(STORAGE_ID));
        verify(genericOper).writeKeystore(eq(user.getUserID()), any(), eq(STORAGE_KEYSTORE), any());
        verify(keystoreCache.getStorageAccess()).remove(user.getUserID());
    }
}
