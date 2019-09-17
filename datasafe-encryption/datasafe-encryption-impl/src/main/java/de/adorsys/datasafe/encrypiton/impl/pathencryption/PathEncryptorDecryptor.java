package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.PathEncryptionSecretKey;

public interface PathEncryptorDecryptor {

    /**
     * @param pathSecretKey entity that contains keys for encrypt path
     * @param originalPath path to encrypt
     * @param associated Associated data to authenticate
     * @return Encrypted path using {@code pathSecretKey}
     */
    byte[] encrypt(PathEncryptionSecretKey pathSecretKey, byte[] originalPath, byte[] associated);

    /**
     * @param pathSecretKey entity that contains keys for decrypt path
     * @param encryptedPath path to decrypt
     * @param associated Associated data to authenticate
     * @return Decrypted path using {@code pathSecretKey}
     */
    byte[] decrypt(PathEncryptionSecretKey pathSecretKey, byte[] encryptedPath, byte[] associated);
}
