package de.adorsys.datasafe.encrypiton.api.keystore;

import java.security.PublicKey;

/**
 * This is responsible for public key serialization/deserialization. As example, user may want PEM based public keys.
 */
public interface PublicKeySerde {

    /**
     * Deserializes public key out of its string representation.
     * @param encoded String representation of key (typically Base64 encoded bytes)
     * @return Deserialized public key
     */
    PublicKey readPubKey(String encoded);

    /**
     * Serializes public key out into string representation.
     * @param publicKey Public key to serialize
     * @return String representation of public key (typically Base64 encoded bytes)
     */
    String writePubKey(PublicKey publicKey);
}
