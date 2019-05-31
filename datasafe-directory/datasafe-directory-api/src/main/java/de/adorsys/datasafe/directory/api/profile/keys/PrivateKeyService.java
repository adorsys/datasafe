package de.adorsys.datasafe.directory.api.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;

import java.security.Key;

/**
 * Acts as a private and secret keys database.
 */
public interface PrivateKeyService {

    /**
     * Get path-encryption key that will be used to encrypt URI paths.
     * @param forUser Key owner
     * @return Path encryption secret key.
     */
    SecretKeyIDWithKey pathEncryptionSecretKey(UserIDAuth forUser);

    /**
     * Get document-encryption key
     * @param forUser Key owner
     * @return Document encryption secret key.
     */
    SecretKeyIDWithKey documentEncryptionSecretKey(UserIDAuth forUser);

    /**
     * Raw access to get key by its ID.
     * @param forUser Key owner
     * @param keyId Key ID
     * @return Key from database/storage associated with {@code keyId}
     */
    Key keyById(UserIDAuth forUser, String keyId);
}
