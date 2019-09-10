package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.*;

/**
 * Retrieves and opens private keystore associated with user location DFS storage.
 * Attempts to re-read keystore if not able to open it.
 */
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
    public SecretKeyIDWithKey pathEncryptionSecretKey(UserIDAuth forUser) {
        List<KeyID> secretKeyIds = getSecretKeyIds(forUser);

        Map<String, Key> keys = keysByIds(forUser, secretKeyIds.stream()
                    .map(it -> it.getValue()).collect(Collectors.toSet()));

        String secretPathKeyId = getSecretPathKeyIdByPrefix(keys, PATH_KEY_ID_PREFIX);
        String secretPathCrtKeyId = getSecretPathKeyIdByPrefix(keys, PATH_KEY_ID_PREFIX_CRT);

        return new SecretKeyIDWithKey(
                    new KeyID(secretPathKeyId),
                    (SecretKey) keyStoreOper.getKey(forUser, secretPathKeyId),
                    (SecretKey) keyStoreOper.getKey(forUser, secretPathCrtKeyId));
    }

    private String getSecretPathKeyIdByPrefix(Map<String, Key> keys, String pathKeyIdPrefix) {
        return keys.keySet()
                   .stream()
                   .filter(it -> it.startsWith(pathKeyIdPrefix))
                   .findFirst()
                   .get();
    }

    private List<KeyID> getSecretKeyIds(UserIDAuth forUser) {
        return keyStoreOper.readAliases(forUser).stream()
                    .filter(it -> it.startsWith(PATH_KEY_ID_PREFIX) || it.startsWith(PATH_KEY_ID_PREFIX_CRT))
                    .map(KeyID::new)
                    .collect(Collectors.toList());
    }

    /**
     * Reads document encryption secret key from DFS and caches the result.
     */
    @Override
    public SecretKeyIDWithKey documentEncryptionSecretKey(UserIDAuth forUser) {
        return keyByPrefix(forUser, DOCUMENT_KEY_ID_PREFIX);
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
                        keyId -> keyStoreOper.getKey(forUser, keyId))
                );
    }

    private SecretKeyIDWithKey keyByPrefix(UserIDAuth forUser, String prefix) {
        KeyID key = keyStoreOper.readAliases(forUser).stream()
                .filter(it -> it.startsWith(prefix))
                .map(KeyID::new)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No key with prefix: " + prefix));

        return new SecretKeyIDWithKey(
                key,
                (SecretKey) keyStoreOper.getKey(forUser, key.getValue()),
                null//TODO
        );
    }
}
