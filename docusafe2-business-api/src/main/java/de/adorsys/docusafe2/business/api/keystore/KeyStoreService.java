package de.adorsys.docusafe2.business.api.keystore;

import de.adorsys.docusafe2.business.api.keystore.types.*;

import java.security.KeyStore;

/**
 * Created by peter on 11.01.18.
 */
public interface KeyStoreService {
    KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                            KeyStoreType keyStoreType,
                            KeyStoreCreationConfig config);
    KeySourceAndKeyID getKeySourceAndKeyIDForPublicKey(KeyStoreAccess keyStoreAccess);

    PublicKeyJWK getPublicKeyJWK(KeyStoreAccess keyStoreAccess);

    KeySource getKeySourceForPrivateKey(KeyStoreAccess keyStoreAccess);

    KeySourceAndKeyID getKeySourceAndKeyIDForSecretKey(KeyStoreAccess keyStoreAccess);

    SecretKeyIDWithKey getRandomSecretKeyIDWithKey(KeyStoreAccess keyStoreAccess, KeyStore userKeystore);

}
