package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.AuthPathEncryptionSecretKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.PATH_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.PATH_KEY_ID_PREFIX_CTR;

/**
 * Retrieves and opens private keystore associated with user location DFS storage.
 * Attempts to re-read keystore if not able to open it.
 */
@Slf4j
@RuntimeDelegate
public class DFSPrivateKeyServiceImpl implements PrivateKeyService {

    private final DocumentKeyStoreOperations keyStoreOper;

    @Inject
    public DFSPrivateKeyServiceImpl(DocumentKeyStoreOperations keyStoreOper) {
        this.keyStoreOper = keyStoreOper;
    }

    /**
     * Reads path encryption secret key from DFS and caches the result.
     */
    @Override
    public AuthPathEncryptionSecretKey pathEncryptionSecretKey(UserIDAuth forUser) {
        SecretKeyIDWithKey secretPathKeyId = getKeyByPrefix(forUser, new PathKeyType());
        SecretKeyIDWithKey secretPathCtrKeyId = getKeyByPrefix(forUser, new PathCtrKeyType());

        return new AuthPathEncryptionSecretKey(secretPathKeyId, secretPathCtrKeyId);
    }

    /**
     * Reads document encryption secret key from DFS and caches the result.
     */
    @Override
    public SecretKeyIDWithKey documentEncryptionSecretKey(UserIDAuth forUser) {
        return getKeyByPrefix(forUser, new DocumentKeyType());
    }

    /**
     * Read users' document access key to validate that he can open his keystore.
     */
    @Override
    @SneakyThrows
    public void validateUserHasAccessOrThrow(UserIDAuth forUser) {
        // avoid only unauthorized access
        try {
            getKeyByPrefix(forUser, new DocumentKeyType()); // for access check
        } catch (RuntimeException ex) {
            // lombok @SneakyThrows handling
            if (ex.getCause() instanceof KeyStoreException ||
                    ex.getCause() instanceof UnrecoverableKeyException ||
                    ex.getCause() instanceof BadPaddingException) {
                throw ex.getCause();
            }

            // It is safe to ignore other types of exceptions - i.e. keystore does not exist
            log.debug("Caught exception while validating keystore access", ex.getCause());
        }
    }

    /**
     * Reads private or secret key from DFS and caches the keystore associated with it.
     */
    @Override
    @SneakyThrows
    public Map<String, Key> keysByIds(UserIDAuth forUser, Set<String> keyIds) {
        Set<String> aliases = keyStoreOper.readAliases(forUser);
        return keyIds.stream()
                .filter(aliases::contains)
                .collect(Collectors.toMap(
                        keyId -> keyId,
                        keyId -> keyStoreOper.getKey(forUser, keyId)
                ));
    }

    @Override
    public KeyPair getKeyPair(UserIDAuth forUser) {
        return keyStoreOper.getKeyPair(forUser);
    }

    /**
     * Retrieves a key from the keystore based on the given key type.
     */
    private SecretKeyIDWithKey getKeyByPrefix(UserIDAuth forUser, KeyType keyType) {
        Collection<String> aliases = keyStoreOper.readAliases(forUser);
        String prefix = keyType.getPrefix();

        KeyID key = aliases.stream()
                .filter(it -> it.startsWith(prefix))
                .map(KeyID::new)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No key with prefix: " + prefix));

        return new SecretKeyIDWithKey(
                key,
                (SecretKey) keyStoreOper.getKey(forUser, key.getValue())
        );
    }

    /**
     * Abstract class representing different key types.
     */
    private abstract static class KeyType {
        public abstract String getPrefix();
    }

    /**
     * Key type for document encryption keys.
     */
    private static class DocumentKeyType extends KeyType {
        @Override
        public String getPrefix() {
            return DOCUMENT_KEY_ID_PREFIX;
        }
    }

    /**
     * Key type for path encryption keys.
     */
    private static class PathKeyType extends KeyType {
        @Override
        public String getPrefix() {
            return PATH_KEY_ID_PREFIX;
        }
    }

    /**
     * Key type for path counter encryption keys.
     */
    private static class PathCtrKeyType extends KeyType {
        @Override
        public String getPrefix() {
            return PATH_KEY_ID_PREFIX_CTR;
        }
    }
}