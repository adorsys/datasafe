package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * Path encryption cipher configurer.
 */
public interface PathEncryptionConfig {

    /**
     * @param secretKey Use this secret key
     * @return Path encryption cipher that uses {@code secretKey}
     */
    Cipher encryptionCipher(SecretKey secretKey);

    /**
     * @param secretKey Use this secret key
     * @return Path decryption cipher that uses {@code secretKey}
     */
    Cipher decryptionCipher(SecretKey secretKey);

    /**
     * Serializes encrypted bytes of document path.
     * @param bytes Encrypted path as bytes
     * @return String representation of encrypted path
     */
    String byteSerializer(byte[] bytes);

    /**
     * Deserializes encrypted bytes of document path.
     * @param input String representation of encrypted path
     * @return byte representation of path suitable for decryption
     */
    byte[] byteDeserializer(String input);
}
