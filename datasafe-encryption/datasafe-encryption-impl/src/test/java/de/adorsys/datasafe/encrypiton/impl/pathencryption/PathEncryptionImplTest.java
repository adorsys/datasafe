package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import javax.crypto.SecretKey;
import java.security.KeyStore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PathEncryptionImplTest extends BaseMockitoTest {
    String uriString = "https://192.168.178.0.1:9090/minio/first/folder";
    private final KeyStoreService keyStoreService = new KeyStoreServiceImpl(
            EncryptionConfig.builder().build().getKeystore(),
            DaggerBCJuggler.builder().build()
    );
    @Mock
    private SymmetricPathEncryptionService symmetricPathEncryptionService;
    @Mock
    private PrivateKeyService privateKeyService;

    PathEncryptionImpl pathEncryption;



    @BeforeEach
    void setUp() {
        pathEncryption = new PathEncryptionImpl(symmetricPathEncryptionService, privateKeyService);
    }

    @Test
    public void testPathEncryption() {
        ReadStorePassword storePassword = new ReadStorePassword("storepass");
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("keypass".toCharArray());
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(storePassword, readKeyPassword);
        KeyID keyID = new KeyID("secret");

        KeyCreationConfig config = KeyCreationConfig.builder().signKeyNumber(0).encKeyNumber(1).build();
        KeyStore keystore = keyStoreService.createKeyStore(keyStoreAuth,config);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keystore, keyStoreAuth);

        SecretKey secretKey = keyStoreService.getSecretKey(keyStoreAccess, keyID);
        SecretKeyIDWithKey secretKeyID = new SecretKeyIDWithKey(keyID, secretKey);

        when(symmetricPathEncryptionService.encrypt(any(), any())).thenReturn(new Uri(uriString + ".enc"));
        when(privateKeyService.pathEncryptionSecretKey(any())).thenReturn(new AuthPathEncryptionSecretKey(secretKeyID,secretKeyID));

        UserID user = new UserID("user1");
        UserIDAuth userAuth = new UserIDAuth(user, readKeyPassword);

        Uri encryptedPath = pathEncryption.encrypt(userAuth, new Uri(uriString));
        Assertions.assertEquals(encryptedPath, new Uri(uriString + ".enc"));
    }
}
