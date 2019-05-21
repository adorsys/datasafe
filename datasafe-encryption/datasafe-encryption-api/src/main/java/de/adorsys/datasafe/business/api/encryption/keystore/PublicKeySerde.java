package de.adorsys.datasafe.business.api.encryption.keystore;

import java.security.PublicKey;

/**
 * This is responsible for public key serialization/deserialization. I.e. user may want PEM based public keys.
 */
public interface PublicKeySerde {

    PublicKey readPubKey(String encoded);
    String writePubKey(PublicKey publicKey);
}
