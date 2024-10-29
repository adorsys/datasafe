import de.adorsys.KeyStoreOper;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.keymanagement.api.Juggler;
import de.adorsys.keymanagement.api.config.keystore.KeyStoreConfig;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.List;

import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class KeyStoreOperTest extends BaseMockitoTest {

    private final UserIDAuth user = new UserIDAuth("John", new ReadKeyPassword("key_pass".toCharArray()));
    private final ReadStorePassword readStorePassword = new ReadStorePassword("store_pass");
    private final DefaultDFSConfig config = new DefaultDFSConfig(new URI("file:///somepath/to/test"), readStorePassword);
    private final KeyCreationConfig keyCreationConfig = KeyCreationConfig.builder().signKeyNumber(0).encKeyNumber(1).build();
    private final KeyStoreService keyStoreService = new KeyStoreServiceImpl(
            EncryptionConfig.builder().build().getKeystore(),
            DaggerBCJuggler.builder().build()
    );
    private KeyStore keystore;
    private KeyStoreAccess keyStoreAccess;
    private byte[] serializedKeystore;
    @InjectMocks
    private KeyStoreOper keyStoreOper;
    @Mock
    private KeyStoreService keyStoreServiceMock;
    @Mock
    private UserPrivateProfile userPrivateProfile;
    @Mock
    private CreateUserPrivateProfile createUserProfile;
    @Mock
    private StorageService storageService;
    @Mock
    private StorageService readService;
    @Mock
    private StorageWriteService writeService;

    public KeyStoreOperTest() throws URISyntaxException {
    }

    @BeforeEach
    void setUp() {
        createUserProfile = config.defaultPrivateTemplate(user);
        userPrivateProfile = createUserProfile.buildPrivateProfile();

        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, user.getReadKeyPassword());
        keystore = keyStoreService.createKeyStore(keyStoreAuth, keyCreationConfig);
        keyStoreAccess = new KeyStoreAccess(keystore, keyStoreAuth);

        KeyStoreConfig keyStoreConfig = KeyStoreConfig.builder().type("UBER").build();
        Juggler juggler = DaggerBCJuggler.builder().keyStoreConfig(keyStoreConfig).build();
        serializedKeystore = juggler.serializeDeserialize().serialize(keystore, readStorePassword::getValue);

        keyStoreOper = new KeyStoreOper(storageService, config, keyCreationConfig, keyStoreServiceMock);
    }

    @SneakyThrows
    @Test
    void createAndWriteKeyStore() {
        OutputStream os = new FileOutputStream(File.createTempFile("testfile", ".txt"));

        when(writeService.write(any())).thenReturn(os);
        when(keyStoreServiceMock.serialize(any(), any())).thenReturn(serializedKeystore);
        when(keyStoreServiceMock.createKeyStore(any(), any())).thenReturn(keystore);

        keyStoreOper.createKeyStore(userPrivateProfile, user);

        verify(keyStoreServiceMock, times(1)).createKeyStore(any(), any());
        verify(writeService, times(1)).write(any());
        verify(keyStoreServiceMock, times(1)).serialize(eq(keystore), eq(readStorePassword));
    }

    @SneakyThrows
    @Test
    void ifKeystoreExistsWillNotBeCreatedAgain() {
        InputStream is = new FileInputStream(File.createTempFile("testfile", ".txt"));

        when(readService.read(any())).thenReturn(is);
        when(keyStoreServiceMock.deserialize(any(), any())).thenReturn(keystore);

        keyStoreOper.createKeyStore(userPrivateProfile, user);

        verify(keyStoreServiceMock, times(0)).createKeyStore(any(), any());
        verify(writeService, times(0)).write(any());
        verify(keyStoreServiceMock, times(0)).serialize(eq(keystore), eq(readStorePassword));
    }

    @SneakyThrows
    @Test
    void getPublickey() {
        List<PublicKeyIDWithPublicKey> publicKeysMock = keyStoreService.getPublicKeys(keyStoreAccess);

        when(keyStoreServiceMock.getPublicKeys(any())).thenReturn(publicKeysMock);

        List<PublicKeyIDWithPublicKey> publicKeys = keyStoreOper.getPublicKey(user, userPrivateProfile);
        Assertions.assertEquals(1, publicKeys.size());
        Assertions.assertEquals(publicKeysMock.get(0).getPublicKey(), publicKeys.get(0).getPublicKey());
    }

    @SneakyThrows
    @Test
    void getPrivateKey() {
        List<PublicKeyIDWithPublicKey> publicKeysMock = keyStoreService.getPublicKeys(keyStoreAccess);
        PrivateKey privateKeyMock = keyStoreService.getPrivateKey(keyStoreAccess, publicKeysMock.get(0).getKeyID());

        when(keyStoreServiceMock.getPublicKeys(any())).thenReturn(publicKeysMock);
        when(keyStoreServiceMock.getPrivateKey(any(), any())).thenReturn(privateKeyMock);

        PrivateKey privateKey = keyStoreOper.getPrivateKey(user, userPrivateProfile);
        Assertions.assertEquals(privateKeyMock, privateKey);

    }

    @SneakyThrows
    @Test
    void getSecretKey() {
        InputStream is = new FileInputStream(File.createTempFile("testfile", ".txt"));
        KeyID keyID = KeyUtilClass.keyIdByPrefix(keystore, DOCUMENT_KEY_ID_PREFIX);
        SecretKey secretKey = keyStoreService.getSecretKey(keyStoreAccess, keyID);


        when(readService.read(any())).thenReturn(is);
        when(keyStoreServiceMock.deserialize(any(), any())).thenReturn(keystore);
        when(keyStoreServiceMock.getSecretKey(any(), any())).thenReturn((SecretKeySpec) secretKey);

        SecretKeyIDWithKey secretKeyIDWithKey = keyStoreOper.getSecretKey(user, userPrivateProfile);
        Assertions.assertEquals(secretKey, secretKeyIDWithKey.getSecretKey());
    }

}
