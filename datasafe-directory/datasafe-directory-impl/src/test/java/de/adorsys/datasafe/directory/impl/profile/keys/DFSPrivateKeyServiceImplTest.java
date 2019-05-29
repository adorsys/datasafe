package de.adorsys.datasafe.directory.impl.profile.keys;

import com.google.common.collect.ImmutableMap;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.resource.*;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import java.security.KeyStore;
import java.util.HashMap;

import static org.mockito.Mockito.when;

class DFSPrivateKeyServiceImplTest extends BaseMockitoTest {

    private static final AbsoluteLocation<PrivateResource> PRIVATE = BasePrivateResource
            .forAbsolutePrivate(new Uri("s3://bucket"));
    private static final AbsoluteLocation<PublicResource> PUBLIC = BasePublicResource
            .forAbsolutePublic(new Uri("s3://bucket"));

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
    private ProfileRetrievalService profile;

    @BeforeEach
    void init() {
        when(profile.publicProfile(auth.getUserID())).thenReturn(publicProfile);
        when(profile.privateProfile(auth)).thenReturn(privateProfile);
        when(keystoreCache.getPrivateKeys()).thenReturn(new HashMap<>(ImmutableMap.of(
                auth.getUserID(),
                privateKeystore))
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
