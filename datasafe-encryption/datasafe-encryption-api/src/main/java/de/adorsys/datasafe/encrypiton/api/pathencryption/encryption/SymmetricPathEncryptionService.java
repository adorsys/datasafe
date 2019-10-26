package de.adorsys.datasafe.encrypiton.api.pathencryption.encryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.AuthPathEncryptionSecretKey;
import de.adorsys.datasafe.types.api.resource.Uri;

/**
 * Encrypts and decrypts relative URI's using symmetric cryptography.
 */
public interface SymmetricPathEncryptionService {

    /**
     * Encrypts relative URI using secret key and serializes it into URL-friendly format.
     * @param pathEncryptionSecretKey entity with keys for encrypt path
     * @param bucketPath Path to encrypt
     * @return Encrypted relative URI that can be safely published.
     */
    Uri encrypt(AuthPathEncryptionSecretKey pathEncryptionSecretKey, Uri bucketPath);

    /**
     * Decrypts relative URI using secret key.
     * @param pathEncryptionSecretKey entity with keys for decrypt path
     * @param bucketPath Path to decrypt
     * @return Decrypted relative URI typically containing some sensitive information.
     */
    Uri decrypt(AuthPathEncryptionSecretKey pathEncryptionSecretKey, Uri bucketPath);

}
