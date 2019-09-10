package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;

public interface PathEncryptorDecryptor {

    /**
     * @param secretKeyEntry Use this secret key
     * @return Encrypted path using {@code secretKey} (secretKey and counterSecretKey)
     */
    byte[] encrypt(SecretKeyIDWithKey secretKeyEntry, byte[] rawData);

    /**
     * @param secretKey Use this secret key
     * @return Decrypted path using {@code secretKey}
     */
    byte[] decrypt(SecretKeyIDWithKey secretKey, byte[] encryptedData);

}
