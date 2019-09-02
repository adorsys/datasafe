package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;

/**
 * Path encryption cipher configurer.
 */
public interface PathEncryptor {

    /**
     * @param secretKeyEntry Use this secret key
     * @return Path encryption cipher that uses {@code secretKey}
     */
    byte[] encrypt(SecretKeyIDWithKey secretKeyEntry, byte[] rawData);

    /**
     * @param secretKey Use this secret key
     * @return Path decryption cipher that uses {@code secretKey}
     */
    byte[] decrypt(SecretKeyIDWithKey secretKey, byte[] encryptedData);

}
