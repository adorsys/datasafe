package de.adorsys.datasafe.simple.adapter.api.legacy.pathencryption;

import de.adorsys.datasafe.types.api.resource.Uri;

import javax.crypto.SecretKey;

/**
 * Encrypts and decrypts relative URI's using symmetric cryptography.
 */
public interface LegacySymmetricPathEncryptionService {

    /**
     * Encrypts relative URI using secret key and serializes it into URL-friendly format.
     * @param secretKey Key to encrypt with
     * @param bucketPath Path to encrypt
     * @return Encrypted relative URI that can be safely published.
     */
    Uri encrypt(SecretKey secretKey, Uri bucketPath);

    /**
     * Decrypts relative URI using secret key.
     * @param secretKey Key to decrypt with
     * @param bucketPath Path to decrypt
     * @return Decrypted relative URI typically containing some sensitive information.
     */
    Uri decrypt(SecretKey secretKey, Uri bucketPath);
}
