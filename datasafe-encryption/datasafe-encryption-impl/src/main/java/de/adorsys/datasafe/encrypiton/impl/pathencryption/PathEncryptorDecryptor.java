package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.PathEncryptionSecretKey;

public interface PathEncryptorDecryptor {

    /**
     * @param pathSecretKey entity that contains keys for encrypt path
     * @return Encrypted path using {@code pathSecretKey}
     */
    byte[] encrypt(PathEncryptionSecretKey pathSecretKey, byte[] rawData);

    /**
     * @param pathSecretKey entity that contains keys for decrypt path
     * @return Decrypted path using {@code pathSecretKey}
     */
    byte[] decrypt(PathEncryptionSecretKey pathSecretKey, byte[] encryptedData);

}
