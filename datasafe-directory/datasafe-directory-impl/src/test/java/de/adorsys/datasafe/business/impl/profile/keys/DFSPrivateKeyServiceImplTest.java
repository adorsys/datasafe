package de.adorsys.datasafe.business.impl.profile.keys;

import com.google.common.collect.ImmutableMap;
import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.actions.StorageReadService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.UserPublicProfile;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.*;
import de.adorsys.datasafe.business.impl.profile.operations.DFSSystem;
import de.adorsys.datasafe.shared.BaseMockitoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;

import static org.mockito.Mockito.when;

class DFSPrivateKeyServiceImplTest extends BaseMockitoTest {

    private static final AbsoluteLocation<PrivateResource> PRIVATE = BasePrivateResource
            .forAbsolutePrivate(URI.create("s3://bucket"));
    private static final AbsoluteLocation<PublicResource> PUBLIC = BasePublicResource
            .forAbsolutePublic(URI.create("s3://bucket"));

    private UserIDAuth auth = new UserIDAuth(new UserID(""), new ReadKeyPassword(""));

    private UserPrivateProfile privateProfile = UserPrivateProfile.builder()
            .privateStorage(PRIVATE)
            .inboxWithFullAccess(PRIVATE)
            .keystore(PRIVATE)
            .documentVersionStorage(PRIVATE)
            .build();

    private UserPublicProfile publicProfile = UserPublicProfile.builder()
            .publicKeys(PUBLIC)
            .inbox(PUBLIC)
            .build();

    @Mock
    private KeyStore privateKeystore;

    @Mock
    private KeyStore publicKeystore;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private KeyStoreCache keystoreCache;

    @Mock
    private KeyStoreService keyStoreService;

    @Mock
    private DFSSystem dfsSystem;

    @Mock
    private BucketAccessService bucketAccessService;

    @Mock
    private ProfileRetrievalService profile;

    @Mock
    private StreamReadUtil streamReadUtil;

    @Mock
    private StorageReadService readService;

    @InjectMocks
    private DFSPrivateKeyServiceImpl privateKeyService;

    @BeforeEach
    void init() {
        when(profile.publicProfile(auth.getUserID())).thenReturn(publicProfile);
        when(profile.privateProfile(auth)).thenReturn(privateProfile);
        when(keystoreCache.getPrivateKeys()).thenReturn(new HashMap<>(ImmutableMap.of(
                auth.getUserID(),
                privateKeystore))
        );

        when(keystoreCache.getPublicKeys()).thenReturn(new HashMap<>(ImmutableMap.of(
                auth.getUserID(),
                publicKeystore))
        );
    }

    @Test
    void pathEncryptionSecretKey() {
        //privateKeyService.pathEncryptionSecretKey(auth);
    }

    @Test
    void documentEncryptionSecretKey() {
        //privateKeyService.documentEncryptionSecretKey(auth);
    }

    @Test
    void keyById() {
        //privateKeyService.documentEncryptionSecretKey(auth);
    }

    @Test
    void keyByIdCaches() {
       // privateKeyService.documentEncryptionSecretKey(auth);
    }
}