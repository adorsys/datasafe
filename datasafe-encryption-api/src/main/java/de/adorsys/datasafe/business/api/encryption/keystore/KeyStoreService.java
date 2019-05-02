package de.adorsys.datasafe.business.api.encryption.keystore;

import de.adorsys.datasafe.business.api.types.keystore.*;

import javax.crypto.spec.SecretKeySpec;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.List;

/**
 * Created by peter on 11.01.18.
 */
public interface KeyStoreService {

    KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                            KeyStoreType keyStoreType,
                            KeyStoreCreationConfig config);

    List<PublicKeyIDWithPublicKey> getPublicKeys(KeyStoreAccess keyStoreAccess);

    PrivateKey getPrivateKey(KeyStoreAccess keyStoreAccess, KeyID keyID);

    SecretKeySpec getSecretKey(KeyStoreAccess keyStoreAccess, KeyID keyID);

    SecretKeyIDWithKey getRandomSecretKeyID(KeyStoreAccess keyStoreAccess);

}
