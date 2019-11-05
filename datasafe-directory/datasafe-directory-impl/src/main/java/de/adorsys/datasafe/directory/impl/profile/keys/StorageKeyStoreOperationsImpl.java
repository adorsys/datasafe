package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Provides different kinds of high-level keystore (that is for STORAGE-CREDENTIALS) operations.
 * It is expected that backing storage service can access storage credentials keystore.
 */
@Slf4j
@RuntimeDelegate
public class StorageKeyStoreOperationsImpl implements StorageKeyStoreOperations {

    private final GsonSerde gson;
    private final KeyStoreService keyStoreService;
    private final GenericKeystoreOperations genericOper;
    private final ProfileRetrievalService profile;
    private final BucketAccessService access;
    private final KeyStoreCache keystoreCache;

    @Inject
    public StorageKeyStoreOperationsImpl(GsonSerde gson, KeyStoreService keyStoreService,
                                         GenericKeystoreOperations genericOper, ProfileRetrievalService profile,
                                         BucketAccessService access, KeyStoreCache keystoreCache) {
        this.gson = gson;
        this.keyStoreService = keyStoreService;
        this.genericOper = genericOper;
        this.profile = profile;
        this.access = access;
        this.keystoreCache = keystoreCache;
    }

    /**
     * Tries to re-read keystore from storage if supplied password can't open cached keystore.
     */
    @Override
    @SneakyThrows
    public StorageCredentials getStorageCredentials(UserIDAuth forUser, StorageIdentifier id) {
        if (null == storageKeystoreLocation(forUser)) {
            return null;
        }

        // Expected to be - PBEKeySpec
        String key = new String(
                genericOper.getKey(() -> keyStore(forUser), forUser, id.getId()).getEncoded(),
                StandardCharsets.UTF_8
        );

        return deserialize(key.toCharArray());
    }

    /**
     * Reads aliases from keystore associated with user.
     */
    @Override
    @SneakyThrows
    public Set<StorageIdentifier> readAliases(UserIDAuth forUser) {
        if (null == storageKeystoreLocation(forUser)) {
            return Collections.emptySet();
        }

        return genericOper.readAliases(keyStore(forUser)).stream()
                .map(StorageIdentifier::new)
                .collect(Collectors.toSet());
    }

    @Override
    public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
        if (null == storageKeystoreLocation(forUser)) {
            return;
        }

        log.debug("Updating users' '{}' storage keystore ReadKeyPassword", forUser.getUserID());
        AbsoluteLocation location = keystoreLocationWithAccess(forUser);
        genericOper.updateReadKeyPassword(keyStore(forUser), location, forUser, newPassword);
    }

    @Override
    public void createAndWriteKeystore(UserIDAuth forUser) {
        AbsoluteLocation location = keystoreLocationWithAccess(forUser);

        genericOper.writeKeystore(
            forUser.getUserID(),
            genericOper.keystoreAuth(forUser),
            location,
            newKeystore(forUser)
        );
    }

    @Override
    public void addStorageCredentials(UserIDAuth forUser, StorageIdentifier storageId, StorageCredentials credentials) {
        modifyAndStoreKeystore(
                forUser,
                keyStoreAccess -> keyStoreService.addPasswordBasedSecretKey(
                        keyStoreAccess,
                        storageId.getId(),
                        serialize(credentials)
                )
        );
    }

    @Override
    public void removeStorageCredentials(UserIDAuth forUser, StorageIdentifier storageId) {
        modifyAndStoreKeystore(
                forUser,
                keyStoreAccess -> keyStoreService.removeKey(
                        keyStoreAccess,
                        storageId.getId()
                )
        );
    }

    @Override
    public void invalidateCache(UserIDAuth forUser) {
        keystoreCache.getStorageAccess().remove(forUser.getUserID());
    }

    @SneakyThrows
    private void modifyAndStoreKeystore(UserIDAuth forUser, Consumer<KeyStoreAccess> keystoreModifier) {
        log.debug("Modifying users' '{}' keystore", forUser.getUserID());
        AbsoluteLocation location = keystoreLocationWithAccess(forUser);

        KeyStoreAccess keystoreWithCreds = new KeyStoreAccess(
                genericOper.readKeyStore(forUser, location),
                genericOper.keystoreAuth(forUser)
        );

        keystoreModifier.accept(keystoreWithCreds);

        genericOper.writeKeystore(
                forUser.getUserID(),
                genericOper.keystoreAuth(forUser),
                location,
                keystoreWithCreds.getKeyStore()
        );

        invalidateCache(forUser);
    }

    @SneakyThrows
    protected KeyStore newKeystore(UserIDAuth forUser) {
        return genericOper.createEmptyKeystore(forUser);
    }

    private StorageCredentials deserialize(char[] data) {
        return gson.fromJson(new String(data), StorageCredentials.class);
    }

    private char[] serialize(StorageCredentials credentials) {
        return gson.toJson(credentials).toCharArray();
    }

    private AbsoluteLocation keystoreLocationWithAccess(UserIDAuth forUser) {
        AbsoluteLocation<PrivateResource> location = storageKeystoreLocation(forUser);

        if (null == location) {
            throw new IllegalStateException("Profile does not have associated storage keystore");
        }

        return this.access.withSystemAccess(location);
    }

    private AbsoluteLocation<PrivateResource> storageKeystoreLocation(UserIDAuth forUser) {
        return profile.privateProfile(forUser).getStorageCredentialsKeystore();
    }

    private KeyStore keyStore(UserIDAuth forUser) {
        return keystoreCache.getStorageAccess().computeIfAbsent(
                forUser.getUserID(),
                userId -> {
                    AbsoluteLocation location = keystoreLocationWithAccess(forUser);
                    return genericOper.readKeyStore(forUser, location);
                }
        );
    }
}
