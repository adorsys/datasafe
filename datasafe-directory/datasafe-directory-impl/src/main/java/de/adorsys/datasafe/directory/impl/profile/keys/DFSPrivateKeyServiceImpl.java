package de.adorsys.datasafe.directory.impl.profile.keys;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.SneakyThrows;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.PATH_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.DOCUMENT_KEY_ID_PREFIX;

/**
 * Retrieves and opens private keystore associated with user location DFS storage.
 */
@RuntimeDelegate
public class DFSPrivateKeyServiceImpl implements PrivateKeyService {

    private final KeyStoreCache keystoreCache;
    private final KeyStoreService keyStoreService;
    private final DFSConfig dfsConfig;
    private final BucketAccessService bucketAccessService;
    private final ProfileRetrievalService profile;
    private final StorageReadService readService;

    @Inject
    public DFSPrivateKeyServiceImpl(KeyStoreCache keystoreCache,
                                    KeyStoreService keyStoreService, DFSConfig dfsConfig,
                                    BucketAccessService bucketAccessService, ProfileRetrievalService profile,
                                    StorageReadService readService) {
        this.keystoreCache = keystoreCache;
        this.keyStoreService = keyStoreService;
        this.dfsConfig = dfsConfig;
        this.bucketAccessService = bucketAccessService;
        this.profile = profile;
        this.readService = readService;
    }

    /**
     * Reads path encryption secret key from DFS and caches the result.
     */
    @Override
    public SecretKeyIDWithKey pathEncryptionSecretKey(UserIDAuth forUser) {
        return keyByPrefix(forUser, PATH_KEY_ID_PREFIX);
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
        KeyStore keyStore = keyStore(forUser);

        Set<String> aliases = readAliases(keyStore);
        return keyIds.stream()
                .filter(aliases::contains)
                .collect(Collectors.toMap(
                        keyId -> keyId,
                        keyId -> getKey(keyStore, keyId, forUser.getReadKeyPassword()))
                );
    }

    private SecretKeyIDWithKey keyByPrefix(UserIDAuth forUser, String prefix) {
        KeyStore keyStore = keyStore(forUser);
        KeyID key = readAliases(keyStore).stream()
                .filter(it -> it.startsWith(prefix))
                .map(KeyID::new)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No key with prefix: " + prefix));

        return new SecretKeyIDWithKey(
              key,
                (SecretKey) getKey(keyStore, key.getValue(), forUser.getReadKeyPassword())
        );
    }

    private KeyStore keyStore(UserIDAuth forUser) {
        return keystoreCache.getKeystore().computeIfAbsent(
                forUser.getUserID(),
                userId -> readKeyStore(forUser)
        );
    }

    @SneakyThrows
    private KeyStore readKeyStore(UserIDAuth forUser) {
        AbsoluteLocation<PrivateResource> access = bucketAccessService.privateAccessFor(
                forUser,
                profile.privateProfile(forUser).getKeystore().getResource()
        );

        byte[] payload;
        try (InputStream is = readService.read(access)) {
            payload = ByteStreams.toByteArray(is);
        }

        return keyStoreService.deserialize(
                payload,
                forUser.getUserID().getValue(),
                dfsConfig.privateKeyStoreAuth(forUser).getReadStorePassword()
        );
    }

    @SneakyThrows
    private Key getKey(KeyStore keyStore, String alias, ReadKeyPassword readKeyPassword) {
        return keyStore.getKey(alias, readKeyPassword.getValue().toCharArray());
    }

    @SneakyThrows
    private Set<String> readAliases(KeyStore keyStore) {
        Set<String> result = new HashSet<>();
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            result.add(aliases.nextElement());
        }

        return result;
    }
}
