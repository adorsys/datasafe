package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.PathEncryptionSecretKey;

public interface PathEncryptorDecryptor {

    /**
     * @param pathSecretKey entity that contains keys for encrypt path
     * @param originalPath
     * @return Encrypted path using {@code pathSecretKey}
     */
    String encrypt(PathEncryptionSecretKey pathSecretKey, String originalPath);

    /**
     * @param pathSecretKey entity that contains keys for decrypt path
     * @param encryptedPath
     * @return Decrypted path using {@code pathSecretKey}
     */
    String decrypt(PathEncryptionSecretKey pathSecretKey, String encryptedPath);

}
