package de.adorsys.datasafe.directory.api.profile.keys;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.AuthPathEncryptionSecretKey;
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
     * Access validation is not necessary - done by this function implicitly.
     * @param forUser Key owner
     * @return Path encryption entity which contain secret keys and key ID.
     */
    AuthPathEncryptionSecretKey pathEncryptionSecretKey(UserIDAuth forUser);

    /**
     * Get document-encryption key. Access validation is not necessary - done by this function implicitly.
     * @param forUser Key owner
     * @return Document encryption secret key.
     */
    SecretKeyIDWithKey documentEncryptionSecretKey(UserIDAuth forUser);

    /**
     * Validates that user has access to his keystore. Should ignore any storage access exceptions
     * to cover cases if keystore does not exist.
     * @param forUser User to validate.
     */
    void validateUserHasAccessOrThrow(UserIDAuth forUser);

    /**
     * Raw access to get key by its ID specialized for getting multiple keys at a time.
     * Access validation is not necessary - done by this function implicitly (but empty result set may indicate
     * some problem).
     * @param forUser Key owner
     * @param keyIds Key IDs to receive keys for
     * @return Map (key id - Key) from database/storage associated with {@code keyId}, those key ids from
     * {@code keyIds} that are missing - they are silently ignored and not returned in result.
     */
    Map<String, Key> keysByIds(UserIDAuth forUser, Set<String> keyIds);
}
