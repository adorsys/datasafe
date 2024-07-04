package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import javax.inject.Inject;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

public class PublicKeySerdeImplTest extends BaseMockitoTest {
    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(
            EncryptionConfig.builder().build().getKeystore(),
            DaggerBCJuggler.builder().build()
    );

    @Test
    public void writeAndReadPubKey(){
        ReadStorePassword readStorePassword = new ReadStorePassword("storepass");
        ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString("keypass");

        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
        KeyCreationConfig config = KeyCreationConfig.builder().signKeyNumber(0).encKeyNumber(1).build();
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, config);

        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        PublicKeySerdeImpl publicKeySerde = mock(PublicKeySerdeImpl.class);

        List<PublicKeyIDWithPublicKey> publicKeys = keyStoreService.getPublicKeys(keyStoreAccess);

        PublicKey publicKey = publicKeys.get(0).getPublicKey();
        String encodedKey = publicKeySerde.writePubKey(publicKey);

        assertThat(encodedKey).isEqualTo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));

        PublicKey readPublicKey = publicKeySerde.readPubKey(encodedKey);

        assertThat(readPublicKey).isEqualTo(publicKey);
    }
}
