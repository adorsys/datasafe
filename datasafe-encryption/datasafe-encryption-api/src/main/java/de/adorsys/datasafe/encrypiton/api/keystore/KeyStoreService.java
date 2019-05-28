package de.adorsys.datasafe.encrypiton.api.keystore;

import de.adorsys.datasafe.encrypiton.api.types.keystore.*;

import javax.crypto.spec.SecretKeySpec;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class is responsible for creating,serializing keystores as well as reading keys from keystores.
 * TODO: Move it into another subproject - it is very heavyweight and used mostly in profile module.
 */
public interface KeyStoreService {

    KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                            KeyStoreType keyStoreType,
                            KeyStoreCreationConfig config);

    KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                            KeyStoreType keyStoreType,
                            KeyStoreCreationConfig config,
                            Map<KeyID, Optional<SecretKeyEntry>> secretKeys);

    List<PublicKeyIDWithPublicKey> getPublicKeys(KeyStoreAccess keyStoreAccess);

    PrivateKey getPrivateKey(KeyStoreAccess keyStoreAccess, KeyID keyID);

    SecretKeySpec getSecretKey(KeyStoreAccess keyStoreAccess, KeyID keyID);

    byte[] serialize(KeyStore store, String storeId, ReadStorePassword password);

    KeyStore deserialize(byte[] payload, String storeId, ReadStorePassword password);
}
