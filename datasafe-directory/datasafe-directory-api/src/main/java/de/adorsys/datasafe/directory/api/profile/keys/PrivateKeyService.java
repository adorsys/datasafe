package de.adorsys.datasafe.directory.api.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;

import java.security.Key;
import java.util.Map;
import java.util.Set;

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
     * Raw access to get key by its ID specialized for getting multiple key ids at a time.
     * @param forUser Key owner
     * @param keyIds Key IDs to receive keys for
     * @return Map (key id - Key) from database/storage associated with {@code keyId}
     */
    Map<String, Key> keysByIds(UserIDAuth forUser, Set<String> keyIds);
}
