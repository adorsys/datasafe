package de.adorsys.datasafe.directory.impl.profile.keys;


import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.security.*;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DFSPrivateKeyServiceImplTest extends BaseMockitoTest {
    @Mock
    private DocumentKeyStoreOperations keyStoreOper;
    @Mock
    private KeyStoreService keyStoreService;
    DFSPrivateKeyServiceImpl privateKeyService;

    @BeforeEach
    public void setUp() {
        privateKeyService = new DFSPrivateKeyServiceImpl(keyStoreOper);
    }

    @Test
    @SneakyThrows
    public void getKeyPair() {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("keypass".toCharArray());
        UserID user = new UserID("user1");
        UserIDAuth userAuth = new UserIDAuth(user, readKeyPassword);

        KeyPairGenerator KeyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair  = KeyGen.generateKeyPair();

        when(keyStoreOper.getKeyPair(any())).thenReturn(keyPair);

        KeyPair keyPair1 = privateKeyService.getKeyPair(userAuth);
        Assertions.assertEquals(keyPair.getPublic(), keyPair1.getPublic());
        Assertions.assertEquals(keyPair.getPrivate(), keyPair1.getPrivate());
    }

}

