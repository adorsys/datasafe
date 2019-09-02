package de.adorsys.datasafe.encrypiton.api.pathencryption.encryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.resource.Uri;

/**
 * Encrypts and decrypts relative URI's using symmetric cryptography.
 */
public interface SymmetricPathEncryptionService {

    /**
     * Encrypts relative URI using secret key and serializes it into URL-friendly format.
     * @param secretKeyEntry Key to encrypt with
     * @param bucketPath Path to encrypt
     * @return Encrypted relative URI that can be safely published.
     */
    Uri encrypt(SecretKeyIDWithKey secretKeyEntry, Uri bucketPath);

    /**
     * Decrypts relative URI using secret key.
     * @param secretKeyEntry Key to decrypt with
     * @param bucketPath Path to decrypt
     * @return Decrypted relative URI typically containing some sensitive information.
     */
    Uri decrypt(SecretKeyIDWithKey secretKeyEntry, Uri bucketPath);

}
